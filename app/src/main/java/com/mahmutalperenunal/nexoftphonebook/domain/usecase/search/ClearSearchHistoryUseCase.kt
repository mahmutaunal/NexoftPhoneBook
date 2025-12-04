package com.mahmutalperenunal.nexoftphonebook.domain.usecase.search

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository

// Use case for clearing all saved search history entries
class ClearSearchHistoryUseCase(
    private val repository: ContactsRepository
) {
    // Executes the clear operation on the repository
    suspend operator fun invoke() {
        repository.clearSearchHistory()
    }
}