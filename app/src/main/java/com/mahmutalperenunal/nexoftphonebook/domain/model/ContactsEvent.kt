package com.mahmutalperenunal.nexoftphonebook.domain.model

sealed class ContactsEvent {
    data class OnSearchQueryChange(val query: String) : ContactsEvent()
    data class OnSearchSubmit(val query: String) : ContactsEvent()
    data class OnContactClick(val contactId: String) : ContactsEvent()
    data class OnDeleteClick(val contactId: String) : ContactsEvent()
    data class OnEditClick(val contactId: String) : ContactsEvent()
    data class OnSearchHistoryItemClick(val query: String) : ContactsEvent()
    object OnRetry : ContactsEvent()
}