package com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result


// Use case for inserting or updating a contact
class UpsertContactUseCase(
    private val repository: ContactsRepository
) {
    // Executes upsert operation and returns the resulting contact
    suspend operator fun invoke(contact: Contact): Result<Contact> {
        return repository.upsertContact(contact)
    }
}