package com.mahmutalperenunal.nexoftphonebook.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mahmutalperenunal.nexoftphonebook.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM contacts ORDER BY firstName COLLATE NOCASE, lastName COLLATE NOCASE")
    fun getContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContacts(contacts: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: String)

    @Query("DELETE FROM contacts")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM contacts
        WHERE (firstName || ' ' || lastName) LIKE :query
           OR firstName LIKE :query
           OR lastName LIKE :query
           OR phoneNumber LIKE :query
        ORDER BY firstName COLLATE NOCASE, lastName COLLATE NOCASE
    """)
    fun searchContactsFlow(query: String): Flow<List<ContactEntity>>

    @Query("UPDATE contacts SET isInDeviceContacts = :isInDeviceContacts WHERE id = :id")
    suspend fun updateDeviceContactFlag(
        id: String,
        isInDeviceContacts: Boolean
    )

    @Query("SELECT * FROM contacts")
    suspend fun getContactsOnce(): List<ContactEntity>
}