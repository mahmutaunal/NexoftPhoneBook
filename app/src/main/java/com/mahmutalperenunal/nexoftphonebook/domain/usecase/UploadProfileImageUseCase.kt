package com.mahmutalperenunal.nexoftphonebook.domain.usecase

import com.mahmutalperenunal.nexoftphonebook.domain.repository.ContactsRepository
import com.mahmutalperenunal.nexoftphonebook.util.Result

class UploadProfileImageUseCase(
    private val repository: ContactsRepository
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return repository.uploadProfileImage(imageBytes, fileName)
    }
}