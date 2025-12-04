package com.mahmutalperenunal.nexoftphonebook.data.local.device

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract

class DeviceContactsManager(
    private val context: Context
) {

    data class DeviceContactSnapshot(
        val normalizedPhone: String,
        val displayNameLower: String
    )

    // Normalizes a phone number by stripping all non-digit characters
    private fun normalizeNumber(raw: String?): String =
        raw?.filter { it.isDigit() } ?: ""

    fun getDeviceContactsSnapshot(): List<DeviceContactSnapshot> {
        val result = mutableListOf<DeviceContactSnapshot>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val nameIndex = it.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )

            while (it.moveToNext()) {
                val number = it.getString(numberIndex)
                val name = it.getString(nameIndex)

                val normalizedPhone = normalizeNumber(number)
                if (normalizedPhone.isEmpty()) continue

                val displayNameLower = (name ?: "")
                    .trim()
                    .lowercase()

                result.add(
                    DeviceContactSnapshot(
                        normalizedPhone = normalizedPhone,
                        displayNameLower = displayNameLower
                    )
                )
            }
        }

        return result
    }

    fun getAllNormalizedPhoneNumbers(): Set<String> {
        val result = mutableSetOf<String>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            while (it.moveToNext()) {
                val number = it.getString(numberIndex)
                val normalized = normalizeNumber(number)
                if (normalized.isNotEmpty()) {
                    result.add(normalized)
                }
            }
        }

        return result
    }

    fun normalizeNumberPublic(raw: String): String = normalizeNumber(raw)

    fun saveContactToDevice(
        firstName: String,
        lastName: String?,
        phoneNumber: String,
        photoBytes: ByteArray? = null
    ) {
        val contentResolver = context.contentResolver

        val ops = ArrayList<ContentProviderOperation>()

        val rawContactInsertIndex = ops.size
        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        val displayName = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .ifBlank { firstName }

        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    rawContactInsertIndex
                )
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                    firstName
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                    lastName
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    displayName
                )
                .build()
        )

        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    rawContactInsertIndex
                )
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    phoneNumber
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )

        if (photoBytes != null) {
            ops.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Photo.PHOTO,
                        photoBytes
                    )
                    .build()
            )
        }

        contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
    }
}