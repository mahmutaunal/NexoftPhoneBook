package com.mahmutalperenunal.nexoftphonebook.presentation.contacts.model

data class ContactUiModel(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val photoUrl: String?,
    val isInDeviceContacts: Boolean
)