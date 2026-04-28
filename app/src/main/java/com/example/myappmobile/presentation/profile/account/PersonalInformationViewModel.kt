package com.example.myappmobile.presentation.profile.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.example.myappmobile.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonalInformationViewModel : ViewModel() {

    private val editorState = MutableStateFlow(PersonalInformationUiState())

    val uiState: StateFlow<PersonalInformationUiState> = combine(
        AppContainer.authRepository.currentUser,
        editorState,
    ) { user, editor ->
        val safeUser = user.toSafeUiUser()
        val shouldHydrate = editor.fullName.isBlank() && editor.email.isBlank() && !editor.isSaving && !editor.hasUserEdits
        if (shouldHydrate && safeUser.isAuthenticated) {
            editor.copy(
                fullName = safeUser.fullName,
                email = safeUser.email,
                phoneNumber = safeUser.phone,
                address = safeUser.address,
                storeName = safeUser.storeName,
                roleLabel = safeUser.role.toRoleLabel(safeUser.isSeller),
                membershipTier = safeUser.membershipTier,
                avatarUrl = resolvedAvatarUrl(editor = editor, fallback = safeUser.avatarUrl),
                isSeller = safeUser.isSeller,
                isLoading = false,
            )
        } else {
            editor.copy(
                roleLabel = safeUser.role.toRoleLabel(safeUser.isSeller),
                membershipTier = safeUser.membershipTier,
                address = if (editor.address.isBlank()) safeUser.address else editor.address,
                storeName = if (editor.storeName.isBlank()) safeUser.storeName else editor.storeName,
                avatarUrl = resolvedAvatarUrl(editor = editor, fallback = safeUser.avatarUrl),
                isSeller = safeUser.isSeller,
                isLoading = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PersonalInformationUiState(),
    )

    fun onFullNameChange(value: String) {
        editorState.update { it.copy(fullName = value, errorMessage = null, successMessage = null, hasUserEdits = true) }
    }

    fun onEmailChange(value: String) {
        editorState.update { it.copy(email = value, errorMessage = null, successMessage = null, hasUserEdits = true) }
    }

    fun onPhoneChange(value: String) {
        editorState.update { it.copy(phoneNumber = value, errorMessage = null, successMessage = null, hasUserEdits = true) }
    }

    fun onAddressChange(value: String) {
        editorState.update { it.copy(address = value, errorMessage = null, successMessage = null, hasUserEdits = true) }
    }

    fun onStoreNameChange(value: String) {
        editorState.update { it.copy(storeName = value, errorMessage = null, successMessage = null, hasUserEdits = true) }
    }

    fun onAvatarSelected(uri: String) {
        editorState.update {
            it.copy(
                avatarUrl = uri,
                errorMessage = null,
                successMessage = null,
                hasUserEdits = true,
            )
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val snapshot = uiState.value
            if (snapshot.fullName.isBlank() || snapshot.email.isBlank()) {
                editorState.update { it.copy(errorMessage = "Full name and email are required.") }
                return@launch
            }

            editorState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val result = AppContainer.authRepository.updateCurrentUserProfile(
                fullName = snapshot.fullName,
                email = snapshot.email,
                phoneNumber = snapshot.phoneNumber,
                address = snapshot.address,
                avatarUrl = snapshot.avatarUrl,
                storeName = snapshot.storeName,
            )

            result.fold(
                onSuccess = {
                    editorState.update {
                        it.copy(
                            isSaving = false,
                            successMessage = "Your FLORA account information has been updated.",
                            hasUserEdits = false,
                        )
                    }
                },
                onFailure = { error ->
                    editorState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Could not update your account information.",
                        )
                    }
                },
            )
        }
    }

    private fun resolvedAvatarUrl(
        editor: PersonalInformationUiState,
        fallback: String,
    ): String = editor.avatarUrl.ifBlank { fallback }

    private fun String.toRoleLabel(isSeller: Boolean): String = when {
        contains("buyer-seller", ignoreCase = true) -> "Buyer & Seller"
        contains("seller", ignoreCase = true) || isSeller -> "Seller"
        contains("buyer", ignoreCase = true) -> "Buyer"
        else -> "Buyer"
    }

    private fun User.toSafeUiUser() = copy(
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phone = phone.orEmpty(),
        address = address.orEmpty(),
        avatarUrl = avatarUrl.orEmpty(),
        role = role.orEmpty(),
        storeName = storeName.orEmpty(),
        verificationStatus = runCatching { verificationStatus }.getOrDefault(SellerApprovalStatus.NOT_VERIFIED),
        sellerApprovalStatus = runCatching { sellerApprovalStatus }.getOrDefault(SellerApprovalStatus.NOT_VERIFIED),
        membershipTier = membershipTier.orEmpty(),
    )
}
