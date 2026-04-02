package com.example.myappmobile.presentation.auth.verification

sealed interface VerificationEvent {
    data class CodeChanged(val value: String) : VerificationEvent
    data object Submit : VerificationEvent
}
