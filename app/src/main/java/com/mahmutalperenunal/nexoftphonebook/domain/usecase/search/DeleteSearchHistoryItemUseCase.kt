package com.mahmutalperenunal.nexoftphonebook.domain.usecase.search

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository

class DeleteSearchHistoryItemUseCase(
    private val repository: ContactsRepository
) {
    suspend operator fun invoke(id: Long) {
        return repository.deleteSearchHistoryItem(id)
    }
}