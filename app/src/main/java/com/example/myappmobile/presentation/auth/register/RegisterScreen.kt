package com.example.myappmobile.presentation.auth.register

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Carpenter
import androidx.compose.material.icons.outlined.ShoppingBasket
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.AccountTypeCard
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraTextSecondary

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            onRegisterSuccess()
            viewModel.onEvent(RegisterEvent.ConsumeRegisterSuccess)
        }
    }

    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(RegisterEvent.ClearError)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "JOIN OUR COMMUNITY",
                style = MaterialTheme.typography.labelSmall,
                color = FloraTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create an\nAccount",
                style = MaterialTheme.typography.displayMedium,
                color = FloraText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (uiState.isArtisan) {
                    "Set up your artisan profile, showcase your craft, and connect with buyers."
                } else {
                    "Discover a world of curated artisan goods crafted for collectors like you."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "ACCOUNT TYPE",
                style = MaterialTheme.typography.labelSmall,
                color = FloraTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccountTypeCard(
                    title = AccountType.BUYER.displayName,
                    subtitle = AccountType.BUYER.subtitle,
                    icon = Icons.Outlined.ShoppingBasket,
                    isSelected = uiState.selectedAccountType == AccountType.BUYER,
                    onClick = {
                        viewModel.onEvent(RegisterEvent.AccountTypeSelected(AccountType.BUYER))
                    },
                    modifier = Modifier.weight(1f)
                )
                AccountTypeCard(
                    title = AccountType.SELLER.displayName,
                    subtitle = AccountType.SELLER.subtitle,
                    icon = Icons.Outlined.Carpenter,
                    isSelected = uiState.selectedAccountType == AccountType.SELLER,
                    onClick = {
                        viewModel.onEvent(RegisterEvent.AccountTypeSelected(AccountType.SELLER))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                label = "FULL NAME",
                value = uiState.fullName,
                onValueChange = { viewModel.onEvent(RegisterEvent.FullNameChanged(it)) },
                placeholder = "Enter your full name",
                imeAction = ImeAction.Next,
                isError = uiState.fullNameError != null,
                errorMessage = uiState.fullNameError
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                label = "EMAIL ADDRESS",
                value = uiState.email,
                onValueChange = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
                placeholder = "email@example.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                label = "PHONE NUMBER",
                value = uiState.phoneNumber,
                onValueChange = { viewModel.onEvent(RegisterEvent.PhoneNumberChanged(it)) },
                placeholder = "Enter your phone number",
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
                isError = uiState.phoneNumberError != null,
                errorMessage = uiState.phoneNumberError
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                label = "PASSWORD",
                value = uiState.password,
                onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
                placeholder = "At least 8 characters",
                isPassword = true,
                passwordVisible = uiState.isPasswordVisible,
                onPasswordToggle = { viewModel.onEvent(RegisterEvent.TogglePasswordVisibility) },
                imeAction = ImeAction.Next,
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                label = "CONFIRM PASSWORD",
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                placeholder = "Re-enter your password",
                isPassword = true,
                passwordVisible = uiState.isConfirmPasswordVisible,
                onPasswordToggle = { viewModel.onEvent(RegisterEvent.ToggleConfirmPasswordVisibility) },
                imeAction = if (uiState.isArtisan) ImeAction.Next else ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.onEvent(RegisterEvent.Register) }
                ),
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError
            )

            if (uiState.isArtisan) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "ARTISAN DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = FloraTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                AuthTextField(
                    label = "STORE NAME",
                    value = uiState.storeName,
                    onValueChange = { viewModel.onEvent(RegisterEvent.StoreNameChanged(it)) },
                    placeholder = "Enter your store name",
                    imeAction = ImeAction.Next,
                    isError = uiState.storeNameError != null,
                    errorMessage = uiState.storeNameError
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    label = "ADDRESS",
                    value = uiState.address,
                    onValueChange = { viewModel.onEvent(RegisterEvent.AddressChanged(it)) },
                    placeholder = "Enter your business address",
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.onEvent(RegisterEvent.Register) }
                    ),
                    isError = uiState.addressError != null,
                    errorMessage = uiState.addressError
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            PrimaryButton(
                text = if (uiState.isArtisan) "CREATE ARTISAN ACCOUNT" else "CREATE ACCOUNT",
                onClick = { viewModel.onEvent(RegisterEvent.Register) },
                isLoading = uiState.isLoading,
                enabled = uiState.isFormValid
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (uiState.isFormValid) {
                    "Everything looks good. You can submit."
                } else if (uiState.isArtisan) {
                    "Add your artisan details and make sure passwords match before submitting."
                } else {
                    "Complete all required fields and make sure passwords match."
                },
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = FloraTextSecondary,
                                fontSize = 14.sp
                            )
                        ) {
                            append("Already part of the atelier?  ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = FloraBrown,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        ) {
                            append("Sign in")
                        }
                    },
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = FloraDivider)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = FloraTextMuted, fontSize = 10.sp)) {
                        append("BY JOINING, YOU AGREE TO OUR ")
                    }
                    withStyle(
                        SpanStyle(
                            color = FloraTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("TERMS OF SERVICE")
                    }
                    withStyle(SpanStyle(color = FloraTextMuted, fontSize = 10.sp)) {
                        append(" AND ")
                    }
                    withStyle(
                        SpanStyle(
                            color = FloraTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("PRIVACY ETHOS")
                    }
                    withStyle(SpanStyle(color = FloraTextMuted, fontSize = 10.sp)) {
                        append(".")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
