package com.mahmutalperenunal.nexoftphonebook.data.remote.dto

data class ApiResponse<T>(
    val success: Boolean,
    val messages: List<String>?,
    val data: T,
    val status: Int
)