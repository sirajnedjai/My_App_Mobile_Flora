package com.example.myappmobile.presentation.profile.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.utils.Validators
import com.example.myappmobile.data.repository.LoginActivityEntry
import com.example.myappmobile.data.repository.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PasswordSecurityUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val twoFactorEnabled: Boolean = false,
    val loginActivity: List<LoginActivityEntry> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSaving: Boolean = false,
)

class PasswordSecurityViewModel : ViewModel() {
    private val editor = MutableStateFlow(PasswordSecurityUiState())

    val uiState: StateFlow<PasswordSecurityUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.accountSettingsRepository.securityPreferences,
        editor,
    ) { user, _, state ->
        val security = AppContainer.accountSettingsRepository.getSecurityPreferences(user.id)
        state.copy(
            twoFactorEnabled = security.twoFactorEnabled,
            loginActivity = AppContainer.accountSettingsRepository.loginActivityFor(user.fullName.ifBlank { "FLORA member" }),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PasswordSecurityUiState(),
    )

    fun onCurrentPasswordChanged(value: String) = editor.update {
        it.copy(
            currentPassword = value,
            currentPasswordError = null,
            errorMessage = null,
            successMessage = null,
        )
    }
    fun onNewPasswordChanged(value: String) = editor.update {
        it.copy(
            newPassword = value,
            newPasswordError = null,
            errorMessage = null,
            successMessage = null,
        )
    }
    fun onConfirmPasswordChanged(value: String) = editor.update {
        it.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            errorMessage = null,
            successMessage = null,
        )
    }

    fun onTwoFactorChanged(enabled: Boolean) {
        val userId = AppContainer.authRepository.currentUser.value.id
        AppContainer.accountSettingsRepository.saveSecurityPreferences(userId, SecurityPreferences(twoFactorEnabled = enabled))
        editor.update { it.copy(twoFactorEnabled = enabled, successMessage = "Security preferences updated.") }
    }

    fun savePassword() {
        val snapshot = uiState.value
        val currentPasswordError = when {
            snapshot.currentPassword.isBlank() -> "Enter your current password."
            else -> null
        }
        val newPasswordError = when {
            snapshot.newPassword.isBlank() -> "Enter a new password."
            !Validators.isValidPassword(snapshot.newPassword) -> "New password must be at least 8 characters."
            snapshot.currentPassword == snapshot.newPassword -> "Choose a new password different from the current one."
            else -> null
        }
        val confirmPasswordError = when {
            snapshot.confirmPassword.isBlank() -> "Confirm your new password."
            snapshot.newPassword != snapshot.confirmPassword -> "New password and confirmation do not match."
            else -> null
        }
        if (currentPasswordError != null || newPasswordError != null || confirmPasswordError != null) {
            editor.update {
                it.copy(
                    currentPasswordError = currentPasswordError,
                    newPasswordError = newPasswordError,
                    confirmPasswordError = confirmPasswordError,
                    errorMessage = null,
                    successMessage = null,
                )
            }
            return
        }
        viewModelScope.launch {
            editor.update {
                it.copy(
                    isSaving = true,
                    currentPasswordError = null,
                    newPasswordError = null,
                    confirmPasswordError = null,
                    errorMessage = null,
                    successMessage = null,
                )
            }
            val result = AppContainer.authRepository.updateCurrentUserPassword(
                currentPassword = snapshot.currentPassword,
                newPassword = snapshot.newPassword,
                confirmPassword = snapshot.confirmPassword,
            )
            result.fold(
                onSuccess = {
                    editor.value = PasswordSecurityUiState(
                        twoFactorEnabled = uiState.value.twoFactorEnabled,
                        loginActivity = uiState.value.loginActivity,
                        successMessage = "Your password has been updated successfully.",
                    )
                },
                onFailure = { error ->
                    editor.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Unable to update password.",
                            successMessage = null,
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun PasswordSecurityScreen(
    onBack: () -> Unit,
    viewModel: PasswordSecurityViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScaffold(title = "Password & Security", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FloraBeige)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SettingsHeroCard(
                    title = "Protect your account with secure credentials and verification methods.",
                    subtitle = "Update your password, strengthen sign-in controls, and review access history.",
                    icon = Icons.Outlined.Lock,
                )
            }
            item {
                SettingsCard("Change Password") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AuthTextField(
                            label = "Current Password",
                            value = uiState.currentPassword,
                            onValueChange = viewModel::onCurrentPasswordChanged,
                            isPassword = true,
                            isError = uiState.currentPasswordError != null,
                            errorMessage = uiState.currentPasswordError,
                        )
                        AuthTextField(
                            label = "New Password",
                            value = uiState.newPassword,
                            onValueChange = viewModel::onNewPasswordChanged,
                            isPassword = true,
                            isError = uiState.newPasswordError != null,
                            errorMessage = uiState.newPasswordError,
                        )
                        AuthTextField(
                            label = "Confirm Password",
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChanged,
                            isPassword = true,
                            isError = uiState.confirmPasswordError != null,
                            errorMessage = uiState.confirmPasswordError,
                        )
                        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        uiState.successMessage?.let { Text(it, color = FloraText, style = MaterialTheme.typography.bodySmall) }
                        PrimaryButton(
                            text = "Update Password",
                            onClick = viewModel::savePassword,
                            isLoading = uiState.isSaving,
                            enabled = !uiState.isSaving,
                        )
                    }
                }
            }
            item {
                SettingsCard("Two-Factor Authentication") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add an extra verification step before FLORA allows access to your account.", style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Enable Two-Factor Authentication", style = MaterialTheme.typography.titleMedium, color = FloraText)
                            Switch(
                                checked = uiState.twoFactorEnabled,
                                onCheckedChange = viewModel::onTwoFactorChanged,
                            )
                        }
                    }
                }
            }
            item {
                Text("Login Activity", style = MaterialTheme.typography.titleLarge, color = FloraText)
            }
            items(uiState.loginActivity, key = LoginActivityEntry::id) { entry ->
                SettingsCard(entry.title) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium, color = FloraText)
                        Text(entry.timestamp, style = MaterialTheme.typography.bodySmall, color = FloraTextSecondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title, style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifFontFamily), color = FloraText)
                },
                navigationIcon = {
                    CircularIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FloraBeige,
                    scrolledContainerColor = FloraBeige,
                ),
            )
        },
        containerColor = FloraBeige,
        content = content,
    )
}

@Composable
internal fun SettingsHeroCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = FloraText)
            Text(title, style = MaterialTheme.typography.titleLarge, color = FloraText)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
        }
    }
}

@Composable
internal fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = FloraText)
            content()
        }
    }
}
