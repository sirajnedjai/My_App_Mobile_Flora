package com.example.myappmobile.presentation.auth.verification

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VerificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun onEvent(event: VerificationEvent) {
        when (event) {
            is VerificationEvent.CodeChanged -> {
                val filtered = event.value.filter { it.isDigit() }.take(6)
                _uiState.update {
                    it.copy(
                        code = filtered,
                        codeError = null,
                        message = null,
                    )
                }
            }

            VerificationEvent.Submit -> verifyCode()
        }
    }

    private fun verifyCode() {
        _uiState.update {
            it.copy(
                codeError = "Verification is not available yet because the backend endpoint is not implemented.",
                isVerified = false,
                message = "Verification codes are not connected to a server in this build.",
            )
        }
    }
}
