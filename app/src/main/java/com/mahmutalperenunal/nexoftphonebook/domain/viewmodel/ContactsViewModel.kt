package com.mahmutalperenunal.nexoftphonebook.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactSectionUiModel
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactUiModel
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactsEvent
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactsState
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.DeleteContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.GetContactsUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.GetSearchHistoryUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.SaveSearchQueryUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.SearchContactsUseCase
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ViewModel that manages contacts list, search, and related UI state
class ContactsViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val saveSearchQueryUseCase: SaveSearchQueryUseCase
) : ViewModel() {

    // Backing state flow for the contacts screen
    private val _state = MutableStateFlow(ContactsState())
    val state: StateFlow<ContactsState> = _state

    init {
        // Start observing contacts and search history on ViewModel creation
        observeContacts()
        observeSearchHistory()
    }

    // Single entry point for UI events related to contacts
    fun onEvent(event: ContactsEvent) {
        when (event) {
            is ContactsEvent.OnSearchQueryChange -> {
                _state.value = _state.value.copy(
                    searchQuery = event.query
                )
            }

            is ContactsEvent.OnSearchSubmit -> {
                performSearch(event.query)
            }

            is ContactsEvent.OnContactClick -> {

            }

            is ContactsEvent.OnDeleteClick -> {
                deleteContact(event.contactId)
            }

            is ContactsEvent.OnEditClick -> {

            }

            is ContactsEvent.OnSearchHistoryItemClick -> {
                val query = event.query
                _state.value = _state.value.copy(searchQuery = query)
                performSearch(query)
            }

            is ContactsEvent.OnRetry -> {
                observeContacts()
            }

            is ContactsEvent.OnAddContactClick -> {

            }
        }
    }

    // Collect contacts from the use case and update UI sections
    private fun observeContacts() {
        viewModelScope.launch {
            getContactsUseCase().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                    }

                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            sections = result.data.toSectionUiModels()
                        )
                    }

                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Beklenmeyen bir hata oluştu"
                        )
                    }
                }
            }
        }
    }

    // Observe saved search history and expose it as a distinct, sorted list
    private fun observeSearchHistory() {
        viewModelScope.launch {
            getSearchHistoryUseCase().collectLatest { items ->
                _state.value = _state.value.copy(
                    searchHistory = items
                        .sortedByDescending { it.createdAt }
                        .map { it.query }
                        .distinct()
                )
            }
        }
    }

    // Save query to history and search contacts by the given text
    private fun performSearch(query: String) {
        viewModelScope.launch {
            saveSearchQueryUseCase(query)

            searchContactsUseCase(query).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                    }

                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            sections = result.data.toSectionUiModels()
                        )
                    }

                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Arama sırasında hata oluştu"
                        )
                    }
                }
            }
        }
    }

    // Delete a contact and expose a simple feedback message
    private fun deleteContact(contactId: String) {
        viewModelScope.launch {
            when (val result = deleteContactUseCase(contactId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        errorMessage = "Kişi silindi"
                    )
                }

                is Result.Error -> {
                    _state.value = _state.value.copy(
                        errorMessage = result.message ?: "Kişi silinirken hata oluştu"
                    )
                }

                Result.Loading -> Unit
            }
        }
    }

    // Convert domain contacts into alphabetically grouped UI sections
    private fun List<Contact>.toSectionUiModels(): List<ContactSectionUiModel> {
        return this
            .sortedBy { it.fullName.lowercase() }
            // Group contacts by first letter of their full name (or # if missing)
            .groupBy { contact ->
                contact.fullName.firstOrNull()?.uppercase() ?: "#"
            }
            .toSortedMap()
            .map { (initial, contacts) ->
                ContactSectionUiModel(
                    title = initial,
                    items = contacts.map { contact ->
                        ContactUiModel(
                            id = contact.id,
                            displayName = contact.fullName,
                            phoneNumber = contact.phoneNumber,
                            photoUrl = contact.photoUrl,
                            isInDeviceContacts = contact.isInDeviceContacts
                        )
                    }
                )
            }
    }
}