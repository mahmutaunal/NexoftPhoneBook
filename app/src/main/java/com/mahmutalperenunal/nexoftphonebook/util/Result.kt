package com.mahmutalperenunal.nexoftphonebook.util

// Generic wrapper for representing success, error, or loading states
sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(
        val message: String? = null,
        val throwable: Throwable? = null
    ) : Result<Nothing>()

    object Loading : Result<Nothing>()
}