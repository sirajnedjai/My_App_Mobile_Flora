package com.example.myappmobile.core.utils

object Validators {
    fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean =
        password.length >= 8

    fun isValidCardNumber(number: String): Boolean =
        number.replace(" ", "").length == 16

    fun isValidPostalCode(code: String): Boolean =
        code.length in 4..10
}