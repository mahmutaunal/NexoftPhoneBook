package com.mahmutalperenunal.nexoftphonebook.domain.usecase.search

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.Flow

// Use case for searching contacts by a given query
class SearchContactsUseCase(
    private val repository: ContactsRepository
) {
    // Returns a flow emitting matching contacts for the query
    operator fun invoke(query: String): Flow<Result<List<Contact>>> {
        return repository.searchContacts(query)
    }
}