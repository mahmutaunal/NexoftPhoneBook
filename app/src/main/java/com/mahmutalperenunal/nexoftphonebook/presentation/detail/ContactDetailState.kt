package com.mahmutalperenunal.nexoftphonebook.presentation.detail

data class ContactDetailState(
    val isLoading: Boolean = false,
    val isNewContact: Boolean = false,
    val isEditMode: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val isSavedInDevice: Boolean = false,
    val showAlreadySavedInfo: Boolean = false,
    val showSuccessToast: String? = null,
    val showDeleteBottomSheet: Boolean = false,
    val errorMessage: String? = null,
    val createCompleted: Boolean = false
) {
    val canSave: Boolean
        get() = firstName.isNotBlank() && phoneNumber.isNotBlank()
}