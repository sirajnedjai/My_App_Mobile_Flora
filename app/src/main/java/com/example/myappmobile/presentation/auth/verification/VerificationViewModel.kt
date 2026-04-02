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
                        codeError = null
                    )
                }
            }

            VerificationEvent.Submit -> verifyCode()
        }
    }

    private fun verifyCode() {
        val code = _uiState.value.code

        if (code.length != 4 && code.length != 6) {
            _uiState.update {
                it.copy(
                    codeError = "Enter a 4-digit or 6-digit code",
                    isVerified = false
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                codeError = null,
                isVerified = true
            )
        }
    }
}
