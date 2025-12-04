package com.mahmutalperenunal.nexoftphonebook.presentation.detail

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.entity.SearchHistoryItem
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.UploadProfileImageUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.DeleteContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.GetContactDetailUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.UpsertContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.device.SaveContactToDeviceUseCase
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Simple unit tests for ContactDetailViewModel to verify
 * basic Event -> State behavior (no Android framework dependencies).
 *
 * These tests focus only on pure state changes (e.g. text fields, edit mode),
 * not on real DB / API integrations.
 */
class ContactDetailViewModelTest {

    @Test
    fun `OnFirstNameChange should update firstName in state`() {
        // Given
        val viewModel = createViewModelWithNoopDependencies()

        // Initial state
        val initialState = viewModel.state.value
        assertEquals("", initialState.firstName)

        // When
        viewModel.onEvent(ContactDetailEvent.OnFirstNameChange("Firstname"))

        // Then
        val newState = viewModel.state.value
        assertEquals("Firstname", newState.firstName)
    }

    @Test
    fun `OnToggleEdit should set edit mode to true`() {
        // Given
        val viewModel = createViewModelWithNoopDependencies()

        val initialState = viewModel.state.value
        assertTrue(initialState.isEditMode)

        // When - toggle edit (in this implementation it always enables edit mode)
        viewModel.onEvent(ContactDetailEvent.OnToggleEdit)
        val afterToggleState = viewModel.state.value

        // Then
        assertTrue(afterToggleState.isEditMode)
    }

    /**
     * Helper to create a ContactDetailViewModel instance with no-op dependencies.
     *
     * NOTE:
     * - We pass isNewContact = true and contactId = null so that init { ... } block
     *   does NOT trigger loadContact().
     * - Use cases are built with a fake repository that returns simple default values.
     */
    private fun createViewModelWithNoopDependencies(): ContactDetailViewModel {
        val fakeRepository = FakeContactsRepository()

        val getContactDetailUseCase = GetContactDetailUseCase(fakeRepository)
        val upsertContactUseCase = UpsertContactUseCase(fakeRepository)
        val deleteContactUseCase = DeleteContactUseCase(fakeRepository)
        val saveContactToDeviceUseCase = SaveContactToDeviceUseCase(fakeRepository)
        val uploadProfileImageUseCase = UploadProfileImageUseCase(fakeRepository)

        return ContactDetailViewModel(
            isNewContact = true,
            contactId = null,
            startInEditMode = true,
            getContactDetailUseCase = getContactDetailUseCase,
            upsertContactUseCase = upsertContactUseCase,
            deleteContactUseCase = deleteContactUseCase,
            saveContactToDeviceUseCase = saveContactToDeviceUseCase,
            uploadProfileImageUseCase = uploadProfileImageUseCase
        )
    }

    /**
     * Minimal fake implementation of ContactsRepository to satisfy use case dependencies.
     * Only the members needed by the use cases are implemented with simple defaults.
     *
     * You can extend this fake later if you want to test more complex behaviors.
     */
    private class FakeContactsRepository : ContactsRepository {
        override fun getContacts(): Flow<Result<List<Contact>>> {
            return flowOf(Result.Success(emptyList()))
        }

        override fun getContactById(id: String): Flow<Result<Contact>> {
            // For detail calls we just return a dummy contact
            val dummy = Contact(
                id = id,
                firstName = "Dummy",
                lastName = "User",
                phoneNumber = "+90 000 000 00 00",
                photoUrl = null,
                isInDeviceContacts = false
            )
            return flowOf(Result.Success(dummy))
        }

        override suspend fun upsertContact(contact: Contact): Result<Contact> {
            // Echo the given contact as success
            return Result.Success(contact)
        }

        override suspend fun deleteContact(id: String): Result<Unit> {
            return Result.Success(Unit)
        }

        override fun searchContacts(query: String): Flow<Result<List<Contact>>> {
            return flowOf(Result.Success(emptyList()))
        }

        override fun getSearchHistory(): Flow<List<SearchHistoryItem>> {
            return flowOf(emptyList())
        }

        override suspend fun saveSearchQuery(query: String) {
            // no-op
        }

        override suspend fun saveContactToDevice(contact: Contact, photoBytes: ByteArray?): Result<Unit> {
            return Result.Success(Unit)
        }

        override suspend fun uploadProfileImage(imageBytes: ByteArray, fileName: String): Result<String> {
            return Result.Success("https://example.com/image.jpg")
        }

        override suspend fun deleteSearchHistoryItem(id: Long) {
            // no-op
        }

        override suspend fun clearSearchHistory() {
            // no-op
        }

        override suspend fun markAsSavedInDevice(contactId: String) {
            // no-op
        }
    }
}