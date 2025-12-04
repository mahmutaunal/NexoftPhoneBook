package com.mahmutalperenunal.nexoftphonebook.data.remote.service

import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.ApiResponse
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.UserDto
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.CreateOrUpdateContactRequest
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.ImageUploadResponse
import com.mahmutalperenunal.nexoftphonebook.data.remote.dto.UsersResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

// Retrofit service defining API endpoints for remote contact operations
interface ContactsApiService {

    @GET("api/User/GetAll")
    suspend fun getContacts(): ApiResponse<UsersResponse>

    @GET("api/User/{id}")
    suspend fun getContactById(
        @Path("id") id: String
    ): ApiResponse<UserDto>

    @POST("api/User")
    suspend fun createContact(
        @Body request: CreateOrUpdateContactRequest
    ): ApiResponse<UserDto>

    @PUT("api/User/{id}")
    suspend fun updateContact(
        @Path("id") id: String,
        @Body request: CreateOrUpdateContactRequest
    ): ApiResponse<UserDto>

    @DELETE("api/User/{id}")
    suspend fun deleteContact(
        @Path("id") id: String
    ): ApiResponse<Map<String, Any>>

    @Multipart
    @POST("api/User/UploadImage")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): ApiResponse<ImageUploadResponse>
}