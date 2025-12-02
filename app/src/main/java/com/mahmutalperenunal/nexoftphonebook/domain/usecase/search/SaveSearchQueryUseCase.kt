package com.mahmutalperenunal.nexoftphonebook.domain.usecase.search

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository

// Use case for saving a search query to history
class SaveSearchQueryUseCase(
    private val repository: ContactsRepository
) {
    // Stores the query if it's not blank
    suspend operator fun invoke(query: String) {
        if (query.isBlank()) return
        repository.saveSearchQuery(query.trim())
    }
}