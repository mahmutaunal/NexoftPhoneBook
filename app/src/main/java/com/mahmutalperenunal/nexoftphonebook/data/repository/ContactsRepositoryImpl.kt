package com.mahmutalperenunal.nexoftphonebook.data.repository

import android.content.ContentResolver
import com.mahmutalperenunal.nexoftphonebook.data.local.db.ContactsDao
import com.mahmutalperenunal.nexoftphonebook.data.local.db.SearchHistoryDao
import com.mahmutalperenunal.nexoftphonebook.data.local.entity.SearchHistoryEntity
import com.mahmutalperenunal.nexoftphonebook.data.mapper.toDomain
import com.mahmutalperenunal.nexoftphonebook.data.mapper.toEntity
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.CreateOrUpdateContactRequest
import com.mahmutalperenunal.nexoftphonebook.data.remote.service.ContactsApiService
import com.mahmutalperenunal.nexoftphonebook.domain.entity.Contact
import com.mahmutalperenunal.nexoftphonebook.domain.entity.SearchHistoryItem
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

// Repository implementation coordinating remote API, local database, and device storage
class ContactsRepositoryImpl(
    private val api: ContactsApiService,
    private val contactsDao: ContactsDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val contentResolver: ContentResolver
) : ContactsRepository {

    override fun getContacts(): Flow<Result<List<Contact>>> = flow {
        emit(Result.Loading)

        try {
            val remoteContacts = api.getContacts()

            contactsDao.clearAll()
            contactsDao.upsertContacts(remoteContacts.map { it.toEntity() })
        } catch (e: Exception) {
            emit(Result.Error(message = e.message, throwable = e))
        }

        contactsDao.getContactsFlow()
            .map { entities ->
                val contacts = entities.map { entity ->
                    entity.toDomain(
                        isInDeviceContacts = false
                    )
                }
                Result.Success(contacts) as Result<List<Contact>>
            }
            .onStart {
                emit(Result.Loading)
            }
            .collect { result ->
                emit(result)
            }
    }

    override fun getContactById(id: String): Flow<Result<Contact>> = flow {
        emit(Result.Loading)

        try {
            val local = contactsDao.getContactById(id)
            if (local != null) {
                emit(Result.Success(local.toDomain(isInDeviceContacts = false)))
            } else {
                val remote = api.getContactById(id)
                contactsDao.upsertContact(remote.toEntity())
                val entity = contactsDao.getContactById(id)
                if (entity != null) {
                    emit(Result.Success(entity.toDomain(isInDeviceContacts = false)))
                } else {
                    emit(Result.Error(message = "Kişi bulunamadı"))
                }
            }
        } catch (e: Exception) {
            emit(Result.Error(message = e.message, throwable = e))
        }
    }

    override suspend fun upsertContact(contact: Contact): Result<Contact> {
        return try {
            val request = CreateOrUpdateContactRequest(
                id = contact.id.ifBlank { null },
                firstName = contact.firstName,
                lastName = contact.lastName,
                phoneNumber = contact.phoneNumber,
                photoUrl = contact.photoUrl
            )

            val dto = if (request.id == null) {
                api.createContact(request)
            } else {
                api.updateContact(request)
            }

            contactsDao.upsertContact(dto.toEntity())

            val entity = contactsDao.getContactById(dto.id)
            val domain = entity?.toDomain(isInDeviceContacts = false)
                ?: contact.copy(id = dto.id)

            Result.Success(domain)
        } catch (e: Exception) {
            Result.Error(message = e.message, throwable = e)
        }
    }

    override suspend fun deleteContact(id: String): Result<Unit> {
        return try {
            api.deleteContact(id)
            contactsDao.deleteContact(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(message = e.message, throwable = e)
        }
    }

    override fun searchContacts(query: String): Flow<Result<List<Contact>>> {
        val likeQuery = "%${query.trim()}%"
        return contactsDao.searchContactsFlow(likeQuery)
            .map { entities ->
                val contacts = entities.map { it.toDomain(isInDeviceContacts = false) }
                Result.Success(contacts) as Result<List<Contact>>
            }
            .onStart {
                emit(Result.Loading)
            }
    }

    override fun getSearchHistory(): Flow<List<SearchHistoryItem>> {
        return searchHistoryDao.getHistoryFlow()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveSearchQuery(query: String) {
        val now = System.currentTimeMillis()
        val entity = SearchHistoryEntity(
            query = query.trim(),
            createdAt = now
        )
        searchHistoryDao.insert(entity)
    }

    override suspend fun saveContactToDevice(contact: Contact): Result<Unit> {
        return Result.Success(Unit)
    }
}