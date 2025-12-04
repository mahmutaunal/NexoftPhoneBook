package com.mahmutalperenunal.nexoftphonebook.data.mapper

import com.mahmutalperenunal.nexoftphonebook.data.local.entity.ContactEntity
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.UserDto
import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact

fun UserDto.toEntity(): ContactEntity =
    ContactEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        photoUrl = profileImageUrl
    )

fun ContactEntity.toDomain(): Contact =
    Contact(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        photoUrl = photoUrl,
        isInDeviceContacts = isInDeviceContacts
    )