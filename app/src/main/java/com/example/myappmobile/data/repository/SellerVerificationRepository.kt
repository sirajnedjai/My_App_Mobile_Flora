package com.example.myappmobile.data.repository

import android.net.Uri
import android.provider.OpenableColumns
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.ApiException
import com.example.myappmobile.data.remote.SellerVerificationApiService
import com.example.myappmobile.data.remote.SellerVerificationStatusDto
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.element
import com.example.myappmobile.data.remote.extractDataElement
import com.example.myappmobile.data.remote.objectAt
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class SellerVerificationData(
    val status: SellerApprovalStatus = SellerApprovalStatus.NOT_VERIFIED,
    val fullName: String = "",
    val phone: String = "",
    val nationalId: String = "",
    val address: String = "",
    val storeName: String = "",
    val description: String = "",
    val documentUrl: String = "",
    val rejectionReason: String? = null,
    val submittedAt: String? = null,
    val reviewedAt: String? = null,
)

data class SellerVerificationSubmission(
    val fullName: String,
    val phone: String,
    val nationalId: String,
    val address: String,
    val storeName: String,
    val description: String,
    val documentUri: String,
)

class SellerVerificationRepository(
    private val sellerVerificationApiService: SellerVerificationApiService,
    private val gson: Gson,
) {
    private val _verificationState = MutableStateFlow(SellerVerificationData())
    val verificationState: StateFlow<SellerVerificationData> = _verificationState.asStateFlow()

    suspend fun refreshVerification(): Result<SellerVerificationData> = runCatching {
        val currentUser = AppContainer.authRepository.currentUser.value
        require(currentUser.isAuthenticated && currentUser.isSeller) {
            "Only authenticated seller accounts can access verification."
        }

        val response = sellerVerificationApiService.getSellerVerification().requireBody(gson)
        val parsed = parseVerificationData(response.data) ?: SellerVerificationData()
        persistSharedStatus(currentUser.id, parsed)
        _verificationState.value = parsed
        parsed
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.toApiException()) },
    )

    suspend fun submitVerification(
        submission: SellerVerificationSubmission,
    ): Result<SellerVerificationData> = runCatching {
        val currentUser = AppContainer.authRepository.currentUser.value
        require(currentUser.isAuthenticated && currentUser.isSeller) {
            "Only authenticated seller accounts can submit verification."
        }

        val response = sellerVerificationApiService.submitSellerVerification(
            body = submission.toPartMap(),
            documents = buildDocumentParts(submission.documentUri),
        ).requireBody(gson)

        val refreshed = refreshVerification().getOrNull()
        val parsed = refreshed
            ?: parseVerificationData(response.data)
            ?: submission.toPendingVerification()

        persistSharedStatus(currentUser.id, parsed)
        persistSubmittedProfile(currentUser.id, submission)
        _verificationState.value = parsed
        parsed
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.toApiException()) },
    )

    private fun parseVerificationData(data: JsonElement?): SellerVerificationData? {
        if (data == null || data.isJsonNull) return null
        val root = data.asObjectOrNull()
        val extracted = extractDataElement(data)?.asObjectOrNull()
        val nested = listOfNotNull(
            root,
            extracted,
            root?.objectAt("verification", "seller_verification", "verification_request", "application", "request"),
            extracted?.objectAt("verification", "seller_verification", "verification_request", "application", "request"),
        )

        val primary = nested.firstOrNull { candidate ->
            candidate.string(
                "status",
                "verification_status",
                "approval_status",
                "seller_status",
                "state",
                "full_name",
                "phone",
                "national_id",
                "document_number",
                "address",
                "store_name",
                "description",
            ) != null || candidate.element("user", "seller", "store") != null
        } ?: root ?: extracted ?: return null

        val user = primary.objectAt("user", "seller")
            ?: root?.objectAt("user", "seller")
            ?: extracted?.objectAt("user", "seller")
        val store = primary.objectAt("store")
            ?: root?.objectAt("store")
            ?: extracted?.objectAt("store")

        val dto = gson.fromJson(primary, SellerVerificationStatusDto::class.java)
        val resolvedStatus = resolveStatus(primary, dto)

        return SellerVerificationData(
            status = resolvedStatus,
            fullName = dto.fullName.orEmpty().ifBlank {
                primary.string("full_name", "name", "owner_name")
                    ?: user?.string("name", "full_name", "owner_name")
                    ?: ""
            },
            phone = dto.phone.orEmpty().ifBlank {
                primary.string("phone", "phone_number")
                    ?: user?.string("phone", "phone_number")
                    ?: ""
            },
            nationalId = dto.nationalId.orEmpty().ifBlank {
                primary.string("national_id", "document_number", "national_id_number", "id_number").orEmpty()
            },
            address = dto.address.orEmpty().ifBlank {
                primary.string("address", "store_address")
                    ?: store?.string("address", "store_address")
                    ?: ""
            },
            storeName = dto.storeName.orEmpty().ifBlank {
                primary.string("store_name", "shop_name")
                    ?: store?.string("name", "store_name", "shop_name")
                    ?: ""
            },
            description = dto.description.orEmpty().ifBlank {
                primary.string("description", "bio", "about")
                    ?: store?.string("description", "bio", "about")
                    ?: ""
            },
            documentUrl = dto.documentUrl.orEmpty().ifBlank {
                primary.string("document_url", "document", "document_file", "verification_document", "document_image").orEmpty()
            },
            rejectionReason = dto.rejectionReason
                ?: primary.string("rejection_reason", "reason", "rejected_reason", "admin_note", "note", "comment"),
            submittedAt = dto.submittedAt ?: primary.string("submitted_at", "created_at", "requested_at"),
            reviewedAt = dto.reviewedAt ?: primary.string("reviewed_at", "updated_at", "approved_at", "rejected_at"),
        )
    }

    private fun resolveStatus(
        primary: JsonObject,
        dto: SellerVerificationStatusDto,
    ): SellerApprovalStatus {
        val normalizedSource = JsonObject().apply {
            primary.entrySet().forEach { (key, value) -> add(key, value) }
            dto.status?.takeIf(String::isNotBlank)?.let { addProperty("status", it) }
        }
        return resolveSellerApprovalStatus(
            primary = normalizedSource,
            includeGenericPendingFallback = true,
        )
    }

    private fun persistSharedStatus(
        sellerId: String,
        verification: SellerVerificationData,
    ) {
        AppContainer.uiPreferencesRepository.saveSellerApprovalStatus(sellerId, verification.status)
    }

    private fun persistSubmittedProfile(
        sellerId: String,
        submission: SellerVerificationSubmission,
    ) {
        val profiles = AppContainer.uiPreferencesRepository.getAccountProfile(sellerId)
        AppContainer.uiPreferencesRepository.saveAccountProfile(
            userId = sellerId,
            profile = profiles.copy(
                fullName = submission.fullName.ifBlank { profiles.fullName },
                phoneNumber = submission.phone.ifBlank { profiles.phoneNumber },
                address = submission.address.ifBlank { profiles.address },
            ),
        )

        val storeConfig = AppContainer.uiPreferencesRepository.getStoreConfiguration(sellerId)
        AppContainer.uiPreferencesRepository.saveStoreConfiguration(
            sellerId = sellerId,
            configuration = storeConfig.copy(
                shopName = submission.storeName.ifBlank { storeConfig.shopName },
                ownerName = submission.fullName.ifBlank { storeConfig.ownerName },
                description = submission.description.ifBlank { storeConfig.description },
            ),
        )
    }

    private fun SellerVerificationSubmission.toPartMap(): Map<String, RequestBody> = buildMap {
        put("full_name", fullName.toPlainTextRequestBody())
        put("phone", phone.toPlainTextRequestBody())
        put("national_id", nationalId.toPlainTextRequestBody())
        put("address", address.toPlainTextRequestBody())
        if (storeName.isNotBlank()) put("store_name", storeName.toPlainTextRequestBody())
        if (description.isNotBlank()) put("description", description.toPlainTextRequestBody())
    }

    private fun SellerVerificationSubmission.toPendingVerification(): SellerVerificationData = SellerVerificationData(
        status = SellerApprovalStatus.PENDING,
        fullName = fullName,
        phone = phone,
        nationalId = nationalId,
        address = address,
        storeName = storeName,
        description = description,
    )

    private fun buildDocumentParts(documentUri: String): List<MultipartBody.Part> {
        if (documentUri.isBlank()) return emptyList()
        val context = AppContainer.applicationContext
        val uri = Uri.parse(documentUri)
        val mimeType = context.contentResolver.getType(uri).orEmpty().ifBlank { "image/*" }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw ApiException("Unable to read the selected verification document.")
        val fileName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }
            .orEmpty()
            .ifBlank { "verification_document" }
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return DOCUMENT_PART_NAMES.map { partName ->
            MultipartBody.Part.createFormData(partName, fileName, requestBody)
        }
    }

    private fun String.toPlainTextRequestBody(): RequestBody = toRequestBody("text/plain".toMediaTypeOrNull())

    private companion object {
        val DOCUMENT_PART_NAMES = listOf("document_file", "document", "verification_document")
    }
}
