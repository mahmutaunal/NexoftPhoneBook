package com.mahmutalperenunal.nexoftphonebook.domain.usecase.search

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository

class ClearSearchHistoryUseCase(
    private val repository: ContactsRepository
) {
    suspend operator fun invoke() {
        repository.clearSearchHistory()
    }
}