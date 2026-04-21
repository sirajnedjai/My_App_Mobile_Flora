package com.example.myappmobile.presentation.profile.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.repository.AccountProfilePreferences
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
        AppContainer.uiPreferencesRepository.accountProfiles,
        editorState,
    ) { user, profiles, editor ->
        val savedProfile = profiles[user.id].orEmpty()
        val shouldHydrate = editor.fullName.isBlank() && editor.email.isBlank() && !editor.isSaving && !editor.hasUserEdits
        if (shouldHydrate && user.isAuthenticated) {
            editor.copy(
                fullName = savedProfile.fullName.ifBlank { user.fullName },
                email = savedProfile.email.ifBlank { user.email },
                phoneNumber = savedProfile.phoneNumber.ifBlank { user.phone },
                address = savedProfile.address,
                roleLabel = if (user.isSeller) "Seller Account" else "Customer Account",
                membershipTier = user.membershipTier,
                avatarUrl = resolvedAvatarUrl(editor = editor, preferences = savedProfile, fallback = user.avatarUrl),
                isLoading = false,
            )
        } else {
            editor.copy(
                roleLabel = if (user.isSeller) "Seller Account" else "Customer Account",
                membershipTier = user.membershipTier,
                address = if (editor.address.isBlank()) savedProfile.address else editor.address,
                avatarUrl = resolvedAvatarUrl(editor = editor, preferences = savedProfile, fallback = user.avatarUrl),
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
            )

            result.fold(
                onSuccess = {
                    AppContainer.uiPreferencesRepository.saveAccountProfile(
                        userId = AppContainer.authRepository.currentUser.value.id,
                        profile = AccountProfilePreferences(
                            fullName = snapshot.fullName.trim(),
                            email = snapshot.email.trim(),
                            phoneNumber = snapshot.phoneNumber.trim(),
                            address = snapshot.address.trim(),
                            avatarUri = snapshot.avatarUrl,
                        ),
                    )
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
        preferences: AccountProfilePreferences,
        fallback: String,
    ): String = editor.avatarUrl.ifBlank { preferences.avatarUri.ifBlank { fallback } }

    private fun AccountProfilePreferences?.orEmpty(): AccountProfilePreferences = this ?: AccountProfilePreferences()
}
