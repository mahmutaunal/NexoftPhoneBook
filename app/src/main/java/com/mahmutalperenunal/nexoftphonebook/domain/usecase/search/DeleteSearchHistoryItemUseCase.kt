package com.mahmutalperenunal.nexoftphonebook.domain.usecase.search

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository

// Use case for deleting a single search history entry by its ID
class DeleteSearchHistoryItemUseCase(
    private val repository: ContactsRepository
) {
    // Executes the delete operation for the given history item ID
    suspend operator fun invoke(id: Long) {
        return repository.deleteSearchHistoryItem(id)
    }
}