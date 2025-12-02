package com.mahmutalperenunal.nexoftphonebook.domain.usecase.device

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result

// Use case for saving a contact into the device's local contacts
class SaveContactToDeviceUseCase(
    private val repository: ContactsRepository
) {
    // Executes the save-to-device operation through the repository
    suspend operator fun invoke(contact: Contact): Result<Unit> {
        return repository.saveContactToDevice(contact)
    }
}