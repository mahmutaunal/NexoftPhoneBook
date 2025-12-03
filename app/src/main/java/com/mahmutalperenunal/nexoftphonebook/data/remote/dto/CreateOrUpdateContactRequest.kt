package com.mahmutalperenunal.nexoftphonebook.data.remote.dto

data class CreateOrUpdateContactRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val profileImageUrl: String?
)