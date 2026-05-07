package com.example.myappmobile.presentation.auth.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val email = uiState.email
    val emailError = when {
        email.isBlank() -> null
        !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address"
        else -> null
    }

    Scaffold(
        topBar = { ForgotPasswordTopBar(onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Reset your password",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your account email. Password reset is currently unavailable in this build.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it.trim()) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Email address") },
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(emailError)
                    }
                },
            )
            uiState.message?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "Not Available Yet",
                onClick = viewModel::onContinueUnavailable,
                enabled = uiState.isValid && emailError == null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Forgot Password") },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
        },
    )
}
