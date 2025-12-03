package com.mahmutalperenunal.nexoftphonebook.data.mapper

import com.mahmutalperenunal.nexoftphonebook.data.local.entity.ContactEntity
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.ContactDto
import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact


fun ContactDto.toEntity(): ContactEntity =
    ContactEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        photoUrl = photoUrl
    )

fun ContactEntity.toDomain(
    isInDeviceContacts: Boolean
): Contact =
    Contact(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        photoUrl = photoUrl,
        isInDeviceContacts = isInDeviceContacts
    )

fun Contact.toEntity(): ContactEntity =
    ContactEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        photoUrl = photoUrl
    )