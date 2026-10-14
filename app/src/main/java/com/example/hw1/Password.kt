package com.example.hw1

import kotlin.random.Random

object PasswordGenerator {

    private const val LATIN = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val ALL = LATIN + DIGITS

    fun generate(minLength: Int = 8): String {
        val length = if (minLength < 8) 8 else minLength
        val chars = StringBuilder(length)

        chars.append(LATIN.random())
        chars.append(DIGITS.random())

        repeat(length - 2) {
            chars.append(ALL.random())
        }

        return chars.toString().toList().shuffled(Random).joinToString("")
    }
}