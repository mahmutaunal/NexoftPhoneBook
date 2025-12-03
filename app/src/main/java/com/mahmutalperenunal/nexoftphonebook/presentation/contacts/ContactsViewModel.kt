package com.mahmutalperenunal.nexoftphonebook.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactSectionUiModel
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactUiModel
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.DeleteContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.GetContactsUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.ClearSearchHistoryUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.DeleteSearchHistoryItemUseCase
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
    private val saveSearchQueryUseCase: SaveSearchQueryUseCase,
    private val deleteSearchHistoryItemUseCase: DeleteSearchHistoryItemUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
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
                val candidate = _state.value.sections
                    .flatMap { it.items }
                    .firstOrNull { it.id == event.contactId }

                _state.value = _state.value.copy(
                    deleteCandidate = candidate,
                    isDeleteSheetVisible = candidate != null
                )
            }

            is ContactsEvent.OnEditClick -> {

            }

            is ContactsEvent.OnSearchHistoryItemClick -> {
                onEvent(ContactsEvent.OnSearchQueryChange(event.query))
                onEvent(ContactsEvent.OnSearchSubmit(event.query))
                _state.value = _state.value.copy(isSearchOverlayVisible = false)
            }

            is ContactsEvent.OnDeleteHistoryItem -> {
                viewModelScope.launch {
                    deleteSearchHistoryItemUseCase(event.id)
                }
            }

            is ContactsEvent.OnRetry -> {
                observeContacts()
            }

            is ContactsEvent.OnAddContactClick -> {

            }

            is ContactsEvent.OnDismissDelete -> {
                _state.value = _state.value.copy(
                    deleteCandidate = null,
                    isDeleteSheetVisible = false
                )
            }

            is ContactsEvent.OnConfirmDelete -> {
                val candidate = _state.value.deleteCandidate ?: return
                deleteContact(candidate.id)
            }

            is ContactsEvent.OnListToastShown -> {
                _state.value = _state.value.copy(successMessage = null)
            }

            is ContactsEvent.OnSearchBarClick -> {
                _state.value = _state.value.copy(isSearchOverlayVisible = true)
            }

            is ContactsEvent.OnSearchOverlayDismiss -> {
                _state.value = _state.value.copy(isSearchOverlayVisible = false)
            }

            is ContactsEvent.OnClearAllHistory -> {
                viewModelScope.launch {
                    clearSearchHistoryUseCase()
                }
                _state.value = _state.value.copy(searchHistory = emptyList())
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
                    searchHistory = items.sortedByDescending { it.createdAt }
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
    private fun deleteContact(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = deleteContactUseCase(id)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isDeleteSheetVisible = false,
                        deleteCandidate = null,
                        successMessage = "User is deleted!"
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isDeleteSheetVisible = false,
                        deleteCandidate = null,
                        errorMessage = result.message ?: "Kişi silinemedi"
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

    class ContactsViewModelFactory(
        private val getContactsUseCase: GetContactsUseCase,
        private val searchContactsUseCase: SearchContactsUseCase,
        private val deleteContactUseCase: DeleteContactUseCase,
        private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
        private val saveSearchQueryUseCase: SaveSearchQueryUseCase,
        private val deleteSearchHistoryItemUseCase: DeleteSearchHistoryItemUseCase,
        private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
                return ContactsViewModel(
                    getContactsUseCase,
                    searchContactsUseCase,
                    deleteContactUseCase,
                    getSearchHistoryUseCase,
                    saveSearchQueryUseCase,
                    deleteSearchHistoryItemUseCase,
                    clearSearchHistoryUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}