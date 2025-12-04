package com.mahmutalperenunal.nexoftphonebook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val photoUrl: String?,
    val isInDeviceContacts: Boolean = false
)