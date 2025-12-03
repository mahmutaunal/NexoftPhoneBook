package com.mahmutalperenunal.nexoftphonebook.data.remote.service

import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.ContactDto
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.CreateOrUpdateContactRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Retrofit service defining API endpoints for remote contact operations
interface ContactsApiService {

    @GET("api/GetAll")
    suspend fun getContacts(): List<ContactDto>

    @GET("api/Get/{id}")
    suspend fun getContactById(
        @Path("id") id: String
    ): ContactDto

    @POST("api/Create")
    suspend fun createContact(
        @Body request: CreateOrUpdateContactRequest
    ): ContactDto

    @PUT("api/Update")
    suspend fun updateContact(
        @Body request: CreateOrUpdateContactRequest
    ): ContactDto

    @DELETE("api/Delete/{id}")
    suspend fun deleteContact(
        @Path("id") id: String
    )
}