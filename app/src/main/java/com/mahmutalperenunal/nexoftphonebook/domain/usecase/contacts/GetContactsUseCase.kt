package com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.Flow

// Use case for retrieving the full contacts list
class GetContactsUseCase(
    private val repository: ContactsRepository
) {
    // Returns a flow emitting the list of contacts
    operator fun invoke(): Flow<Result<List<Contact>>> {
        return repository.getContacts()
    }
}