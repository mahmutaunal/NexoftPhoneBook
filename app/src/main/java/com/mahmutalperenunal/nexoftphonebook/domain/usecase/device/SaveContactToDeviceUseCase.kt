package com.mahmutalperenunal.nexoftphonebook.domain.usecase.device

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result

class SaveContactToDeviceUseCase(
    private val contactsRepository: ContactsRepository
) {
    suspend operator fun invoke(contact: Contact, photoBytes: ByteArray?): Result<Unit> {
        return contactsRepository.saveContactToDevice(contact, photoBytes)
    }
}