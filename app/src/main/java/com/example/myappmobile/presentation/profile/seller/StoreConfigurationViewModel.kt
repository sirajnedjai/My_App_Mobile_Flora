package com.example.myappmobile.presentation.profile.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.repository.StoreConfigurationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class StoreConfigurationViewModel : ViewModel() {

    private val editorState = MutableStateFlow(StoreConfigurationUiState())

    val uiState: StateFlow<StoreConfigurationUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.storeConfigurations,
        AppContainer.uiPreferencesRepository.accountProfiles,
        AppContainer.uiPreferencesRepository.sellerApprovalStatuses,
        editorState,
    ) { user, storeConfigs, profiles, approvalStatuses, editor ->
        val preferences = AppContainer.uiPreferencesRepository.getStoreConfiguration(user.id)
        val accountProfile = profiles[user.id]
        val accountName = accountProfile?.fullName.orEmpty()
        val accountAvatar = accountProfile?.avatarUri.orEmpty()
        val approvalStatus = approvalStatuses[AppContainer.uiPreferencesRepository.normalizeSellerStoreId(user.id)]
            ?: AppContainer.uiPreferencesRepository.getSellerApprovalStatus(user.id)
        val shouldHydrate = !editor.hasUserEdits && !editor.isSaving
        if (shouldHydrate) {
            editor.copy(
                shopName = editor.shopName.ifBlank {
                    preferences.shopName.ifBlank { if (user.isSeller) "${user.fullName}'s Atelier" else "FLORA Atelier" }
                },
                establishmentDate = editor.establishmentDate.ifBlank { preferences.establishmentDate },
                ownerName = editor.ownerName.ifBlank {
                    preferences.ownerName.ifBlank { accountName.ifBlank { user.fullName } }
                },
                logoUri = resolvedLogoUri(editor, preferences, accountAvatar.ifBlank { user.avatarUrl }),
                description = editor.description.ifBlank {
                    preferences.description.ifBlank {
                        "Describe your craft, materials, and what makes your FLORA storefront special."
                    }
                },
                approvalStatus = approvalStatus,
                isLoading = false,
            )
        } else {
            editor.copy(
                logoUri = resolvedLogoUri(editor, preferences, accountAvatar.ifBlank { user.avatarUrl }),
                approvalStatus = approvalStatus,
                isLoading = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreConfigurationUiState(),
    )

    fun onShopNameChange(value: String) = updateEditor { copy(shopName = value, hasUserEdits = true) }

    fun onEstablishmentDateChange(value: String) = updateEditor {
        copy(establishmentDate = value, hasUserEdits = true)
    }

    fun onOwnerNameChange(value: String) = updateEditor { copy(ownerName = value, hasUserEdits = true) }

    fun onDescriptionChange(value: String) = updateEditor { copy(description = value, hasUserEdits = true) }

    fun onLogoSelected(uri: String) = updateEditor { copy(logoUri = uri, hasUserEdits = true) }

    fun saveStoreConfiguration() {
        val snapshot = uiState.value
        if (snapshot.shopName.isBlank() || snapshot.ownerName.isBlank()) {
            updateEditor {
                copy(
                    errorMessage = "Shop name and owner name are required.",
                    successMessage = null,
                )
            }
            return
        }

        updateEditor { copy(isSaving = true, errorMessage = null, successMessage = null) }
        AppContainer.uiPreferencesRepository.saveStoreConfiguration(
            sellerId = AppContainer.authRepository.currentUser.value.id,
            configuration = StoreConfigurationPreferences(
                shopName = snapshot.shopName.trim(),
                establishmentDate = snapshot.establishmentDate.trim(),
                ownerName = snapshot.ownerName.trim(),
                logoUri = snapshot.logoUri,
                description = snapshot.description.trim(),
            ),
        )
        updateEditor {
            copy(
                isSaving = false,
                successMessage = "Store configuration saved successfully.",
                hasUserEdits = false,
            )
        }
    }

    private fun updateEditor(transform: StoreConfigurationUiState.() -> StoreConfigurationUiState) {
        editorState.update { current -> current.transform() }
    }

    private fun resolvedLogoUri(
        editor: StoreConfigurationUiState,
        preferences: StoreConfigurationPreferences,
        fallback: String,
    ): String = editor.logoUri.ifBlank { preferences.logoUri.ifBlank { fallback } }
}
