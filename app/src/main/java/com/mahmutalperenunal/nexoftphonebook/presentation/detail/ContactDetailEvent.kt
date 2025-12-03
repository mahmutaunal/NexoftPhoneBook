package com.mahmutalperenunal.nexoftphonebook.presentation.detail

sealed class ContactDetailEvent {
    data class Init(val contactId: String?) : ContactDetailEvent()

    data class OnFirstNameChange(val value: String) : ContactDetailEvent()
    data class OnLastNameChange(val value: String) : ContactDetailEvent()
    data class OnPhoneNumberChange(val value: String) : ContactDetailEvent()
    data class OnPhotoUrlChange(val value: String) : ContactDetailEvent()
    // ContactDetailEvent.kt
    data class OnImageUploadRequested(val imageBytes: ByteArray, val fileName: String) : ContactDetailEvent()

    object OnToggleEdit : ContactDetailEvent()
    object OnCancelEdit : ContactDetailEvent()
    object OnSaveClick : ContactDetailEvent()

    object OnSaveToPhoneClick : ContactDetailEvent()

    object OnDeleteClick : ContactDetailEvent()
    object OnDeleteConfirm : ContactDetailEvent()
    object OnDeleteCancel : ContactDetailEvent()

    object OnToastShown : ContactDetailEvent()
}