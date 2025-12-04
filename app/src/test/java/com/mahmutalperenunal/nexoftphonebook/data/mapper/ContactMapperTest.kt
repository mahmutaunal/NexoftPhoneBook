package com.mahmutalperenunal.nexoftphonebook.data.mapper

import com.mahmutalperenunal.nexoftphonebook.data.local.entity.ContactEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactMapperTest {

    @Test
    fun `toDomain should map ContactEntity to Contact correctly`() {
        // Given
        val entity = ContactEntity(
            id = "123",
            firstName = "Firstname",
            lastName = "Lastname",
            phoneNumber = "+90 555 555 55 55",
            photoUrl = "https://example.com/photo.jpg",
            isInDeviceContacts = true
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals("123", domain.id)
        assertEquals("Firstname", domain.firstName)
        assertEquals("Lastname", domain.lastName)
        assertEquals("+90 555 555 55 55", domain.phoneNumber)
        assertEquals("https://example.com/photo.jpg", domain.photoUrl)
        assertEquals(true, domain.isInDeviceContacts)
    }
}