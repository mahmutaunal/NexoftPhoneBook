package com.mahmutalperenunal.nexoftphonebook.domain.entity

data class Contact(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val photoUrl: String?,
    val isInDeviceContacts: Boolean
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}