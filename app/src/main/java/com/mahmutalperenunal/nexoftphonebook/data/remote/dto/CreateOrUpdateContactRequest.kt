package com.mahmutalperenunal.nexoftphonebook.data.remote.dto

data class CreateOrUpdateContactRequest(
    val id: String?,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val photoUrl: String?
)