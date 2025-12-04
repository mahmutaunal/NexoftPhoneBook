package com.mahmutalperenunal.nexoftphonebook.data.repository

import com.mahmutalperenunal.nexoftphonebook.data.local.db.ContactsDao
import com.mahmutalperenunal.nexoftphonebook.data.local.db.SearchHistoryDao
import com.mahmutalperenunal.nexoftphonebook.data.local.device.DeviceContactsManager
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Repository implementation coordinating remote API, local database, and device storage
class ContactsRepositoryImpl(
    private val api: ContactsApiService,
    private val contactsDao: ContactsDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val deviceContactsManager: DeviceContactsManager
) : ContactsRepository {

    override fun getContacts(): Flow<Result<List<Contact>>> = flow {
        emit(Result.Loading)

        try {
            val existingEntities = contactsDao.getContactsOnce()
            val existingById = existingEntities.associateBy { it.id }

            var deviceContacts: List<DeviceContactsManager.DeviceContactSnapshot>
            var canReadDeviceContacts: Boolean

            try {
                deviceContacts = deviceContactsManager.getDeviceContactsSnapshot()
                canReadDeviceContacts = true
            } catch (_: SecurityException) {
                deviceContacts = emptyList()
                canReadDeviceContacts = false
            }

            val deviceIndex: Set<Pair<String, String>> = deviceContacts
                .map { it.normalizedPhone to it.displayNameLower }
                .toSet()

            val response = api.getContacts()
            if (response.success) {
                val remoteContacts = response.data.users

                val mergedEntities = remoteContacts.map { dto ->
                    val base = dto.toEntity()
                    val existing = existingById[base.id]

                    val preservedFlag = existing?.isInDeviceContacts ?: base.isInDeviceContacts

                    val remoteDisplayName = listOfNotNull(
                        base.firstName,
                        base.lastName
                    )
                        .joinToString(" ")
                        .trim()
                        .lowercase()

                    val normalizedPhone = deviceContactsManager.normalizeNumberPublic(base.phoneNumber)

                    val inDeviceByPhoneAndName =
                        normalizedPhone.isNotEmpty() &&
                                remoteDisplayName.isNotEmpty() &&
                                deviceIndex.contains(
                                    normalizedPhone to remoteDisplayName
                                )

                    val finalFlag = if (canReadDeviceContacts) {
                        inDeviceByPhoneAndName
                    } else {
                        preservedFlag
                    }

                    base.copy(isInDeviceContacts = finalFlag)
                }

                contactsDao.clearAll()
                contactsDao.upsertContacts(mergedEntities)
            } else {
                emit(
                    Result.Error(
                        message = response.messages?.joinToString().orEmpty()
                    )
                )
            }
        } catch (e: Exception) {
            emit(Result.Error(message = e.message, throwable = e))
        }

        contactsDao.getContactsFlow()
            .map { entities ->
                val contacts = entities.map { it.toDomain() }
                Result.Success(contacts) as Result<List<Contact>>
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
                emit(Result.Success(local.toDomain()))
                return@flow
            }

            val response = api.getContactById(id)
            if (response.success) {
                val dto = response.data

                val existing = contactsDao.getContactById(dto.id)
                val baseEntity = dto.toEntity()
                val mergedEntity = baseEntity.copy(
                    isInDeviceContacts = existing?.isInDeviceContacts ?: baseEntity.isInDeviceContacts
                )

                contactsDao.upsertContact(mergedEntity)

                val entity = contactsDao.getContactById(dto.id)
                if (entity != null) {
                    emit(Result.Success(entity.toDomain()))
                } else {
                    emit(Result.Error(message = "Person Not Found"))
                }
            } else {
                emit(
                    Result.Error(
                        message = response.messages?.joinToString().orEmpty()
                    )
                )
            }
        } catch (e: Exception) {
            emit(Result.Error(message = e.message, throwable = e))
        }
    }

    override suspend fun upsertContact(contact: Contact): Result<Contact> {
        return try {
            val request = CreateOrUpdateContactRequest(
                firstName = contact.firstName,
                lastName = contact.lastName,
                phoneNumber = contact.phoneNumber,
                profileImageUrl = contact.photoUrl
            )

            val response = if (contact.id.isBlank()) {
                api.createContact(request)
            } else {
                api.updateContact(contact.id, request)
            }

            if (!response.success) {
                return Result.Error(
                    message = response.messages?.joinToString().orEmpty()
                )
            }

            val dto = response.data

            val existing = contactsDao.getContactById(dto.id)
            val baseEntity = dto.toEntity()
            val mergedEntity = baseEntity.copy(
                isInDeviceContacts = existing?.isInDeviceContacts ?: baseEntity.isInDeviceContacts
            )

            contactsDao.upsertContact(mergedEntity)

            val entity = contactsDao.getContactById(dto.id)
            val domain = entity?.toDomain() ?: contact.copy(id = dto.id)

            Result.Success(domain)
        } catch (e: Exception) {
            Result.Error(message = e.message, throwable = e)
        }
    }

    override suspend fun deleteContact(id: String): Result<Unit> {
        return try {
            val response = api.deleteContact(id)
            if (!response.success) {
                return Result.Error(
                    message = response.messages?.joinToString().orEmpty()
                )
            }
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
                val contacts = entities.map { it.toDomain() }
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

    override suspend fun saveContactToDevice(contact: Contact, photoBytes: ByteArray?): Result<Unit> {
        return try {
            deviceContactsManager.saveContactToDevice(
                firstName = contact.firstName,
                lastName = contact.lastName,
                phoneNumber = contact.phoneNumber,
                photoBytes = photoBytes
            )

            contactsDao.updateDeviceContactFlag(
                id = contact.id,
                isInDeviceContacts = true
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(message = e.message, throwable = e)
        }
    }

    override suspend fun uploadProfileImage(
        imageBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return try {
            val requestBody = imageBytes.toRequestBody(
                "image/*".toMediaTypeOrNull()
            )
            val part = MultipartBody.Part.createFormData(
                name = "image",
                filename = fileName,
                body = requestBody
            )

            val response = api.uploadImage(part)

            if (response.success) {
                Result.Success(response.data.imageUrl)
            } else {
                Result.Error(response.messages?.joinToString(", ") ?: "Upload failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Upload failed")
        }
    }

    override suspend fun deleteSearchHistoryItem(id: Long) {
        searchHistoryDao.deleteById(id)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    override suspend fun markAsSavedInDevice(contactId: String) {
        contactsDao.updateDeviceContactFlag(
            id = contactId,
            isInDeviceContacts = true
        )
    }
}