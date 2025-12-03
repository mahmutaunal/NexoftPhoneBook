package com.mahmutalperenunal.nexoftphonebook.data.remote.dto

data class UploadImageResponseDto(
    val success: Boolean,
    val messages: List<String>?,
    val data: ImageUploadResponse?,
    val status: Int
)