package com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.Flow


// Use case for retrieving detailed contact information by ID
class GetContactDetailUseCase(
    private val repository: ContactsRepository
) {
    // Returns a flow emitting the contact detail result
    operator fun invoke(id: String): Flow<Result<Contact>> {
        return repository.getContactById(id)
    }
}