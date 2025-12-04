package com.mahmutalperenunal.nexoftphonebook.domain.usecase

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result

// Use case for uploading a profile image and returning its URL
class UploadProfileImageUseCase(
    private val repository: ContactsRepository
) {
    // Uploads image bytes with the given file name through the repository
    suspend operator fun invoke(
        imageBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return repository.uploadProfileImage(imageBytes, fileName)
    }
}