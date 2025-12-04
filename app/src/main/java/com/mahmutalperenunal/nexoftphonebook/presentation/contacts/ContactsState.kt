package com.mahmutalperenunal.nexoftphonebook.presentation.contacts

import com.mahmutalperenunal.nexoftphonebook.domain.entity.SearchHistoryItem
import com.mahmutalperenunal.nexoftphonebook.presentation.contacts.model.ContactSectionUiModel
import com.mahmutalperenunal.nexoftphonebook.presentation.contacts.model.ContactUiModel

data class ContactsState(
    val isLoading: Boolean = false,
    val sections: List<ContactSectionUiModel> = emptyList(),
    val searchQuery: String = "",
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    val errorMessage: String? = null,
    val deleteCandidate: ContactUiModel? = null,
    val isDeleteSheetVisible: Boolean = false,
    val successMessage: String? = null,
    val isSearchOverlayVisible: Boolean = false,
)