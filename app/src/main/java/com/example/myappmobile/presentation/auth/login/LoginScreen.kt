package com.example.myappmobile.presentation.auth.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.SocialButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily

private const val LOGIN_SCREEN_TAG = "LOGIN_DEBUG"

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe login success
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            Log.d(LOGIN_SCREEN_TAG, "Login success observed, navigating")
            runCatching { onLoginSuccess() }
                .onFailure { throwable ->
                    Log.e(LOGIN_SCREEN_TAG, "Navigation after login failed", throwable)
                    viewModel.onEvent(
                        LoginEvent.LoginFailed(
                            throwable.message ?: "Unable to open the home screen."
                        )
                    )
                }
            viewModel.onEvent(LoginEvent.ConsumeLoginSuccess)
        }
    }

    // Show general errors via snackbar
    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(LoginEvent.ClearError)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FloraBeige)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Brand Name — "FLORA" serif italic
            Text(
                text = "FLORA",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 30.sp
                ),
                color = FloraText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displayMedium,
                color = FloraText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Enter your credentials to access your curated space.",
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field
            AuthTextField(
                label = "EMAIL ADDRESS",
                value = uiState.email,
                onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                placeholder = "email@example.com",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Password Field with Forgot Password
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PASSWORD",
                        style = MaterialTheme.typography.labelSmall,
                        color = FloraTextSecondary
                    )
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = FloraBrown,
                        modifier = Modifier.clickable {
                            viewModel.onEvent(LoginEvent.NavigateToForgotPassword)
                            onNavigateToForgotPassword()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                AuthTextField(
                    label = "",
                    value = uiState.password,
                    onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                    isPassword = true,
                    passwordVisible = uiState.isPasswordVisible,
                    onPasswordToggle = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) },
                    imeAction = ImeAction.Done,
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Sign In Button
            PrimaryButton(
                text = "SIGN IN",
                onClick = {
                    Log.d(LOGIN_SCREEN_TAG, "Sign In button clicked")
                    viewModel.onEvent(LoginEvent.SignIn)
                },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR CONTINUE WITH",
                    style = MaterialTheme.typography.labelSmall,
                    color = FloraTextSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialButton(
                    text = "Apple",
                    onClick = { viewModel.onEvent(LoginEvent.SignInWithApple) },
                    icon = Icons.Outlined.Person,
                    modifier = Modifier.weight(1f)
                )
                SocialButton(
                    text = "Google",
                    onClick = { viewModel.onEvent(LoginEvent.SignInWithGoogle) },
                    // Use a custom Google painter or substitute icon here
                    // For now using a placeholder; swap with your Google icon painter
                    icon = null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Navigate to Register
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = FloraTextSecondary,
                            fontSize = 14.sp
                        )
                    ) {
                        append("New to the atelier?  ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = FloraBrown,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    ) {
                        append("Create account")
                    }
                },
                modifier = Modifier.clickable {
                    viewModel.onEvent(LoginEvent.NavigateToRegister)
                    onNavigateToRegister()
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
