package com.example.hw1

object Correct {

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )
    private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")

    fun isValidEmail(value: String): Boolean =
        EMAIL_REGEX.matches(value.trim())

    fun isValidPassword(value: String): Boolean =
        PASSWORD_REGEX.matches(value)

    fun isEmpty(value: String): Boolean = value.isBlank()
}