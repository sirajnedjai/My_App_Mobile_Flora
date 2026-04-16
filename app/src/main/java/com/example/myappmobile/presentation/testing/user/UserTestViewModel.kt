package com.example.myappmobile.presentation.testing.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.local.room.entity.UserEntity
import com.example.myappmobile.data.local.room.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserTestUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val users: List<UserEntity> = emptyList(),
)

class UserTestViewModel : ViewModel() {
    private val repository = UserRepository(DatabaseProvider.getDatabase().userDao())
    private val formState = MutableStateFlow(UserTestUiState())

    val uiState: StateFlow<UserTestUiState> = combine(
        formState,
        repository.getAllUsers(),
    ) { form, users ->
        form.copy(users = users)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserTestUiState(),
    )

    fun onNameChange(value: String) {
        formState.value = formState.value.copy(name = value)
    }

    fun onEmailChange(value: String) {
        formState.value = formState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        formState.value = formState.value.copy(password = value)
    }

    fun onPhoneChange(value: String) {
        formState.value = formState.value.copy(phone = value)
    }

    fun addUser() {
        val current = formState.value
        if (current.name.isBlank() || current.email.isBlank() || current.password.isBlank()) return

        viewModelScope.launch {
            repository.insertUser(
                UserEntity(
                    name = current.name.trim(),
                    email = current.email.trim(),
                    password = current.password,
                    phone = current.phone.trim().ifBlank { null },
                ),
            )
            formState.value = UserTestUiState(users = uiState.value.users)
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }
}
