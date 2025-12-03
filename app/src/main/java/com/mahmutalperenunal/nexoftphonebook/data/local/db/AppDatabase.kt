package com.mahmutalperenunal.nexoftphonebook.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mahmutalperenunal.nexoftphonebook.data.local.entity.ContactEntity
import com.mahmutalperenunal.nexoftphonebook.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        ContactEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}