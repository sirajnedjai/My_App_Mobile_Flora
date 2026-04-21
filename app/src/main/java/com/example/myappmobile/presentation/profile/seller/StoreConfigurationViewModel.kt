package com.example.myappmobile.presentation.profile.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.ApiException
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.data.repository.SellerVerificationSubmission
import com.example.myappmobile.domain.model.SellerApprovalStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreConfigurationViewModel : ViewModel() {

    private val editorState = MutableStateFlow(StoreConfigurationUiState())

    init {
        refreshVerificationStatus()
    }

    val uiState: StateFlow<StoreConfigurationUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.storeConfigurations,
        AppContainer.uiPreferencesRepository.accountProfiles,
        AppContainer.sellerVerificationRepository.verificationState,
        editorState,
    ) { user, storeConfigs, profiles, verification, editor ->
        val preferences = storeConfigs[AppContainer.uiPreferencesRepository.normalizeSellerStoreId(user.id)]
            ?: AppContainer.uiPreferencesRepository.getStoreConfiguration(user.id)
        val accountProfile = profiles[user.id]
            ?: AppContainer.uiPreferencesRepository.getAccountProfile(user.id)
        val shouldHydrate = !editor.hasUserEdits && !editor.isSaving
        val resolvedLogo = editor.logoUri.ifBlank {
            accountProfile.avatarUri.ifBlank { preferences.logoUri.ifBlank { user.avatarUrl } }
        }
        val hydrated = editor.copy(
            fullName = if (shouldHydrate) {
                verification.fullName.ifBlank { accountProfile.fullName.ifBlank { user.fullName } }
            } else {
                editor.fullName
            },
            phoneNumber = if (shouldHydrate) {
                verification.phone.ifBlank { accountProfile.phoneNumber.ifBlank { user.phone } }
            } else {
                editor.phoneNumber
            },
            nationalId = if (shouldHydrate) verification.nationalId else editor.nationalId,
            address = if (shouldHydrate) {
                verification.address.ifBlank { accountProfile.address }
            } else {
                editor.address
            },
            shopName = if (shouldHydrate) {
                verification.storeName.ifBlank {
                    preferences.shopName.ifBlank {
                        if (user.isSeller) "${user.fullName}'s Atelier" else "FLORA Atelier"
                    }
                }
            } else {
                editor.shopName
            },
            description = if (shouldHydrate) {
                verification.description.ifBlank {
                    preferences.description.ifBlank {
                        "Describe your craft, materials, and what makes your FLORA storefront special."
                    }
                }
            } else {
                editor.description
            },
            logoUri = resolvedLogo,
            documentUri = if (shouldHydrate) {
                verification.documentUrl.ifBlank { editor.documentUri }
            } else {
                editor.documentUri
            },
            approvalStatus = verification.status,
            rejectionReason = verification.rejectionReason,
            submittedAt = verification.submittedAt,
            reviewedAt = verification.reviewedAt,
            isLoading = false,
            isSeller = user.isSeller,
        )
        if (shouldHydrate && hydrated.documentUri.isNotBlank() && hydrated.documentLabel.isBlank()) {
            hydrated.copy(documentLabel = hydrated.documentUri.substringAfterLast('/'))
        } else {
            hydrated
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreConfigurationUiState(),
    )

    fun onFullNameChange(value: String) = updateEditor {
        copy(
            fullName = value,
            hasUserEdits = true,
            successMessage = null,
            fieldErrors = fieldErrors.copy(fullName = null),
        )
    }

    fun onPhoneNumberChange(value: String) = updateEditor {
        copy(
            phoneNumber = value,
            hasUserEdits = true,
            successMessage = null,
            fieldErrors = fieldErrors.copy(phoneNumber = null),
        )
    }

    fun onNationalIdChange(value: String) = updateEditor {
        copy(
            nationalId = value,
            hasUserEdits = true,
            successMessage = null,
            fieldErrors = fieldErrors.copy(nationalId = null),
        )
    }

    fun onAddressChange(value: String) = updateEditor {
        copy(
            address = value,
            hasUserEdits = true,
            successMessage = null,
            fieldErrors = fieldErrors.copy(address = null),
        )
    }

    fun onShopNameChange(value: String) = updateEditor {
        copy(
            shopName = value,
            hasUserEdits = true,
            successMessage = null,
            fieldErrors = fieldErrors.copy(shopName = null),
        )
    }

    fun onDescriptionChange(value: String) = updateEditor {
        copy(
            description = value,
            hasUserEdits = true,
            successMessage = null,
            fieldErrors = fieldErrors.copy(description = null),
        )
    }

    fun onDocumentSelected(uri: String, label: String) = updateEditor {
        copy(
            documentUri = uri,
            documentLabel = label,
            hasUserEdits = true,
            successMessage = null,
            fieldErrors = fieldErrors.copy(document = null),
        )
    }

    fun submitVerification() {
        val snapshot = uiState.value
        if (!snapshot.isSeller) {
            updateEditor { copy(errorMessage = "Only seller accounts can submit verification.", successMessage = null) }
            return
        }
        if (snapshot.isSaving || snapshot.approvalStatus == SellerApprovalStatus.PENDING) return
        if (snapshot.approvalStatus == SellerApprovalStatus.APPROVED) {
            updateEditor { copy(errorMessage = null, successMessage = "Your seller verification is already approved.") }
            return
        }

        val fieldErrors = validate(snapshot)
        if (fieldErrors.hasErrors()) {
            updateEditor { copy(fieldErrors = fieldErrors, errorMessage = null, successMessage = null) }
            return
        }

        viewModelScope.launch {
            updateEditor {
                copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                    fieldErrors = SellerVerificationFieldErrors(),
                )
            }
            AppContainer.sellerVerificationRepository.submitVerification(
                SellerVerificationSubmission(
                    fullName = snapshot.fullName.trim(),
                    phone = snapshot.phoneNumber.trim(),
                    nationalId = snapshot.nationalId.trim(),
                    address = snapshot.address.trim(),
                    storeName = snapshot.shopName.trim(),
                    description = snapshot.description.trim(),
                    documentUri = snapshot.documentUri,
                ),
            ).fold(
                onSuccess = { verification ->
                    updateEditor {
                        copy(
                            isSaving = false,
                            approvalStatus = verification.status,
                            rejectionReason = verification.rejectionReason,
                            submittedAt = verification.submittedAt,
                            reviewedAt = verification.reviewedAt,
                            successMessage = when (verification.status) {
                                SellerApprovalStatus.UNKNOWN -> null
                                SellerApprovalStatus.PENDING -> "Verification submitted, under review."
                                SellerApprovalStatus.APPROVED -> "Seller verification approved."
                                SellerApprovalStatus.REJECTED -> "Verification was updated. Please review the latest status."
                                SellerApprovalStatus.NOT_VERIFIED -> null
                            },
                            errorMessage = null,
                            hasUserEdits = false,
                        )
                    }
                },
                onFailure = { error ->
                    val apiError = error.toApiException()
                    updateEditor {
                        copy(
                            isSaving = false,
                            errorMessage = apiError.message,
                            successMessage = null,
                            fieldErrors = apiError.toFieldErrors(),
                        )
                    }
                },
            )
        }
    }

    fun refreshVerificationStatus() {
        viewModelScope.launch {
            updateEditor { copy(isLoading = true, errorMessage = null) }
            AppContainer.sellerVerificationRepository.refreshVerification().fold(
                onSuccess = {
                    updateEditor { copy(isLoading = false, errorMessage = null) }
                },
                onFailure = { error ->
                    updateEditor {
                        copy(
                            isLoading = false,
                            errorMessage = error.toApiException().message,
                        )
                    }
                },
            )
        }
    }

    private fun validate(state: StoreConfigurationUiState): SellerVerificationFieldErrors {
        val trimmedPhone = state.phoneNumber.trim()
        return SellerVerificationFieldErrors(
            fullName = "Enter the full legal name on your verification document.".takeIf { state.fullName.isBlank() },
            phoneNumber = "Enter a valid phone number.".takeIf {
                trimmedPhone.isBlank() || PHONE_REGEX.matches(trimmedPhone).not()
            },
            nationalId = "Enter your national ID or document number.".takeIf { state.nationalId.isBlank() },
            address = "Enter the business or residence address used for verification.".takeIf { state.address.isBlank() },
            shopName = "Enter your store name.".takeIf { state.shopName.isBlank() },
            document = "Select a verification document before submitting.".takeIf { state.documentUri.isBlank() },
        )
    }

    private fun ApiException.toFieldErrors(): SellerVerificationFieldErrors = SellerVerificationFieldErrors(
        fullName = validationErrors["full_name"]?.firstOrNull()
            ?: validationErrors["name"]?.firstOrNull(),
        phoneNumber = validationErrors["phone"]?.firstOrNull()
            ?: validationErrors["phone_number"]?.firstOrNull(),
        nationalId = validationErrors["national_id"]?.firstOrNull()
            ?: validationErrors["document_number"]?.firstOrNull()
            ?: validationErrors["national_id_number"]?.firstOrNull(),
        address = validationErrors["address"]?.firstOrNull()
            ?: validationErrors["store_address"]?.firstOrNull(),
        shopName = validationErrors["store_name"]?.firstOrNull()
            ?: validationErrors["shop_name"]?.firstOrNull(),
        description = validationErrors["description"]?.firstOrNull(),
        document = validationErrors["document"]?.firstOrNull()
            ?: validationErrors["document_file"]?.firstOrNull()
            ?: validationErrors["verification_document"]?.firstOrNull(),
    )

    private fun updateEditor(transform: StoreConfigurationUiState.() -> StoreConfigurationUiState) {
        editorState.update { current -> current.transform() }
    }

    private companion object {
        val PHONE_REGEX = Regex("^\\+?[0-9 ()-]{7,20}$")
    }
}
