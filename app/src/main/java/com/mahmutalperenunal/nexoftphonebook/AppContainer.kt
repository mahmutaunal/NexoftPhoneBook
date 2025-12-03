package com.mahmutalperenunal.nexoftphonebook

import android.content.Context
import androidx.room.Room
import com.mahmutalperenunal.nexoftphonebook.data.local.db.AppDatabase
import com.mahmutalperenunal.nexoftphonebook.data.remote.service.ContactsApiService
import com.mahmutalperenunal.nexoftphonebook.data.repository.ContactsRepositoryImpl
import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.UploadProfileImageUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.DeleteContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.GetContactDetailUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.GetContactsUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.contacts.UpsertContactUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.device.SaveContactToDeviceUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.GetSearchHistoryUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.SaveSearchQueryUseCase
import com.mahmutalperenunal.nexoftphonebook.domain.usecase.search.SearchContactsUseCase
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Manual dependency container providing Retrofit, Room, and use cases
class AppContainer(context: Context) {
    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .addHeader("ApiKey", BuildConfig.API_KEY)
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val contactsApiService: ContactsApiService =
        retrofit.create(ContactsApiService::class.java)

    private val appDatabase: AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "phonebook.db"
        ).build()

    private val contactsDao = appDatabase.contactsDao()
    private val searchHistoryDao = appDatabase.searchHistoryDao()

    private val contactsRepository: ContactsRepository =
        ContactsRepositoryImpl(
            api = contactsApiService,
            contactsDao = contactsDao,
            searchHistoryDao = searchHistoryDao,
            contentResolver = context.contentResolver
        )

    val getContactsUseCase = GetContactsUseCase(contactsRepository)
    val searchContactsUseCase = SearchContactsUseCase(contactsRepository)
    val deleteContactUseCase = DeleteContactUseCase(contactsRepository)
    val getSearchHistoryUseCase = GetSearchHistoryUseCase(contactsRepository)
    val saveSearchQueryUseCase = SaveSearchQueryUseCase(contactsRepository)
    val getContactDetailUseCase = GetContactDetailUseCase(contactsRepository)
    val upsertContactUseCase = UpsertContactUseCase(contactsRepository)
    val saveContactToDeviceUseCase = SaveContactToDeviceUseCase(contactsRepository)
    // AppContainer.kt
    val uploadProfileImageUseCase = UploadProfileImageUseCase(contactsRepository)
}