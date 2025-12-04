package com.mahmutalperenunal.nexoftphonebook.domain.usecase.device

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result

// Use case for saving a contact (and optional photo) into the device's local contacts
class SaveContactToDeviceUseCase(
    private val contactsRepository: ContactsRepository
) {
    // Delegates saving the contact and photo bytes to the repository
    suspend operator fun invoke(contact: Contact, photoBytes: ByteArray?): Result<Unit> {
        return contactsRepository.saveContactToDevice(contact, photoBytes)
    }
}