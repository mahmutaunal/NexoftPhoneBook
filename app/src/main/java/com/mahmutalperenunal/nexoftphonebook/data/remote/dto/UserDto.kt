package com.mahmutalperenunal.nexoftphonebook.data.remote.dto

data class UserDto(
    val id: String,
    val createdAt: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val profileImageUrl: String?
)