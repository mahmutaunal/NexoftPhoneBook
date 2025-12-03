package com.mahmutalperenunal.nexoftphonebook.presentation.contacts

sealed class ContactsEvent {
    data class OnSearchQueryChange(val query: String) : ContactsEvent()
    data class OnSearchSubmit(val query: String) : ContactsEvent()
    data class OnContactClick(val contactId: String) : ContactsEvent()
    data class OnDeleteClick(val contactId: String) : ContactsEvent()
    data class OnEditClick(val contactId: String) : ContactsEvent()
    data class OnSearchHistoryItemClick(val query: String) : ContactsEvent()
    data class OnDeleteHistoryItem(val id: Long) : ContactsEvent()
    object OnRetry : ContactsEvent()
    object OnAddContactClick : ContactsEvent()
    object OnConfirmDelete : ContactsEvent()
    object OnDismissDelete : ContactsEvent()
    object OnListToastShown : ContactsEvent()
    object OnSearchBarClick : ContactsEvent()
    object OnSearchOverlayDismiss : ContactsEvent()
    object OnClearAllHistory : ContactsEvent()
}