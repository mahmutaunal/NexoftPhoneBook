package com.mahmutalperenunal.nexoftphonebook.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.UploadProfileImageUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.DeleteContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.GetContactDetailUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.UpsertContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.device.SaveContactToDeviceUseCase
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ViewModel that manages contact detail, edit, delete, device save, and image upload state
class ContactDetailViewModel(
    private val isNewContact: Boolean,
    private val contactId: String?,
    startInEditMode: Boolean,
    private val getContactDetailUseCase: GetContactDetailUseCase,
    private val upsertContactUseCase: UpsertContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val saveContactToDeviceUseCase: SaveContactToDeviceUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase
) : ViewModel() {

    // Backing state flow for the contact detail screen
    private val _state = MutableStateFlow(
        ContactDetailState(
            isNewContact = isNewContact,
            isEditMode = isNewContact || startInEditMode
        )
    )
    val state: StateFlow<ContactDetailState> = _state

    private var latestPhotoBytes: ByteArray? = null

    init {
        if (!isNewContact && contactId != null) {
            loadContact(contactId)
        }
    }

    // Single entry point for UI events on the contact detail screen
    fun onEvent(event: ContactDetailEvent) {
        when (event) {
            is ContactDetailEvent.Init -> Unit

            is ContactDetailEvent.OnFirstNameChange ->
                _state.value = _state.value.copy(firstName = event.value)

            is ContactDetailEvent.OnLastNameChange ->
                _state.value = _state.value.copy(lastName = event.value)

            is ContactDetailEvent.OnPhoneNumberChange ->
                _state.value = _state.value.copy(phoneNumber = event.value)

            is ContactDetailEvent.OnPhotoUrlChange ->
                _state.value = _state.value.copy(photoUrl = event.value)

            is ContactDetailEvent.OnImageUploadRequested -> {
                latestPhotoBytes = event.imageBytes
                uploadImage(event.imageBytes, event.fileName)
            }

            ContactDetailEvent.OnToggleEdit ->
                _state.value = _state.value.copy(isEditMode = true)

            ContactDetailEvent.OnCancelEdit -> {
                if (_state.value.isNewContact) {
                    _state.value = _state.value.copy(
                        firstName = "",
                        lastName = "",
                        phoneNumber = "",
                        photoUrl = null,
                        isEditMode = true
                    )
                } else {
                    contactId?.let { loadContact(it) }
                    _state.value = _state.value.copy(isEditMode = false)
                }
            }

            ContactDetailEvent.OnSaveClick -> saveContact()

            ContactDetailEvent.OnSaveToPhoneClick -> saveToPhone()

            ContactDetailEvent.OnDeleteClick ->
                _state.value = _state.value.copy(showDeleteBottomSheet = true)

            ContactDetailEvent.OnDeleteConfirm -> deleteContact()

            ContactDetailEvent.OnDeleteCancel ->
                _state.value = _state.value.copy(showDeleteBottomSheet = false)

            ContactDetailEvent.OnToastShown ->
                _state.value = _state.value.copy(showSuccessToast = null)
        }
    }

    // Loads contact details by ID and updates UI state
    private fun loadContact(id: String) {
        viewModelScope.launch {
            getContactDetailUseCase(id).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                    }
                    is Result.Success -> {
                        val contact = result.data
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            firstName = contact.firstName,
                            lastName = contact.lastName,
                            phoneNumber = contact.phoneNumber,
                            photoUrl = contact.photoUrl,
                            isSavedInDevice = contact.isInDeviceContacts,
                            showAlreadySavedInfo = contact.isInDeviceContacts
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "The person could not be loaded"
                        )
                    }
                }
            }
        }
    }

    // Creates or updates the contact using the upsert use case
    private fun saveContact() {
        val current = _state.value
        if (!current.canSave) return

        viewModelScope.launch {
            val existingId = contactId ?: ""
            val contact = Contact(
                id = existingId,
                firstName = current.firstName.trim(),
                lastName = current.lastName.trim(),
                phoneNumber = current.phoneNumber.trim(),
                photoUrl = current.photoUrl,
                isInDeviceContacts = current.isSavedInDevice
            )

            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = upsertContactUseCase(contact)) {
                is Result.Success -> {
                    val isCreate = existingId.isBlank()

                    _state.value = if (isCreate) {
                        _state.value.copy(
                            isLoading = false,
                            isNewContact = true,
                            isEditMode = false,
                            createCompleted = true,
                            showSuccessToast = null
                        )
                    } else {
                        _state.value.copy(
                            isLoading = false,
                            isNewContact = false,
                            isEditMode = false,
                            createCompleted = false,
                            showSuccessToast = "User is updated!"
                        )
                    }
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "An error occurred while saving"
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // Deletes the current contact and shows a feedback message
    private fun deleteContact() {
        val id = contactId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = deleteContactUseCase(id)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        showDeleteBottomSheet = false,
                        showSuccessToast = "User is deleted!"
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        showDeleteBottomSheet = false,
                        errorMessage = result.message ?: "An error occurred while deleting"
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // Saves the current contact (and photo if available) into device contacts
    private fun saveToPhone() {
        val current = _state.value
        if (current.isSavedInDevice) return

        viewModelScope.launch {
            val contact = Contact(
                id = contactId ?: "",
                firstName = current.firstName.trim(),
                lastName = current.lastName.trim(),
                phoneNumber = current.phoneNumber.trim(),
                photoUrl = current.photoUrl,
                isInDeviceContacts = current.isSavedInDevice
            )

            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = saveContactToDeviceUseCase(contact, latestPhotoBytes)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSavedInDevice = true,
                        showAlreadySavedInfo = true,
                        showSuccessToast = "User is added to your phone!"
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Could not be saved to the device"
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // Uploads a profile image and updates the photo URL on success
    private fun uploadImage(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = uploadProfileImageUseCase(imageBytes, fileName)) {
                is Result.Success -> {
                    val url = result.data
                    _state.value = _state.value.copy(
                        isLoading = false,
                        photoUrl = url
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Image upload failed"
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // Factory for creating ContactDetailViewModel instances with required dependencies
    class ContactDetailViewModelFactory(
        private val isNewContact: Boolean,
        private val contactId: String?,
        private val startInEditMode: Boolean,
        private val getContactDetailUseCase: GetContactDetailUseCase,
        private val upsertContactUseCase: UpsertContactUseCase,
        private val deleteContactUseCase: DeleteContactUseCase,
        private val saveContactToDeviceUseCase: SaveContactToDeviceUseCase,
        private val uploadProfileImageUseCase: UploadProfileImageUseCase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactDetailViewModel::class.java)) {
                return ContactDetailViewModel(
                    isNewContact,
                    contactId,
                    startInEditMode,
                    getContactDetailUseCase,
                    upsertContactUseCase,
                    deleteContactUseCase,
                    saveContactToDeviceUseCase,
                    uploadProfileImageUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}