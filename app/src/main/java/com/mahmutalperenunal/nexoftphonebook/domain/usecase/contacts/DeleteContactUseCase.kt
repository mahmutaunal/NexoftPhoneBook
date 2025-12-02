package com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result

// Use case for deleting a contact by its ID
class DeleteContactUseCase(
    private val repository: ContactsRepository
) {
    // Executes the delete operation through the repository
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteContact(id)
    }
}