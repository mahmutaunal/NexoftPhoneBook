package com.mahmutalperenunal.nexoftphonebook.domain.usecase.search

import com.mahmutalperenunal.nexoftphonebook.domain.entity.SearchHistoryItem
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow

// Use case for retrieving saved search history items
class GetSearchHistoryUseCase(
    private val repository: ContactsRepository
) {
    // Returns a flow emitting the list of search history entries
    operator fun invoke(): Flow<List<SearchHistoryItem>> {
        return repository.getSearchHistory()
    }
}