package com.mahmutalperenunal.nexoftphonebook.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.DeleteContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.GetContactsUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.GetSearchHistoryUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.SaveSearchQueryUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.SearchContactsUseCase

// Factory for creating ViewModel instances with required use cases
class ViewModelFactory(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val saveSearchQueryUseCase: SaveSearchQueryUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            return ContactsViewModel(
                getContactsUseCase,
                searchContactsUseCase,
                deleteContactUseCase,
                getSearchHistoryUseCase,
                saveSearchQueryUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}