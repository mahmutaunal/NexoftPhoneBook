package com.mahmutalperenunal.nexoftphonebook.domain.repository

import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.entity.SearchHistoryItem
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {

    fun getContacts(): Flow<Result<List<Contact>>>
    fun getContactById(id: String): Flow<Result<Contact>>
    suspend fun upsertContact(contact: Contact): Result<Contact>
    suspend fun deleteContact(id: String): Result<Unit>
    fun searchContacts(query: String): Flow<Result<List<Contact>>>
    fun getSearchHistory(): Flow<List<SearchHistoryItem>>
    suspend fun saveSearchQuery(query: String)
    suspend fun saveContactToDevice(contact: Contact): Result<Unit>
    suspend fun uploadProfileImage(imageBytes: ByteArray, fileName: String): Result<String>
    suspend fun deleteSearchHistoryItem(id: Long)
    suspend fun clearSearchHistory()
}