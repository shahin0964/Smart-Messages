package com.smartmessages.app.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactSuggestion(
    val name: String,
    val number: String,
    val photoUri: String? = null
)

class ContactsRepository(private val context: Context) {

    suspend fun getContactSuggestions(query: String = ""): List<ContactSuggestion> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().lowercase()
        val digitsQuery = cleanQuery.replace(Regex("[^0-9]"), "")

        if (cleanQuery.isBlank()) {
            return@withContext emptyList()
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }

        val results = mutableListOf<ContactSuggestion>()
        val seenNumbers = mutableSetOf<String>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"
            )

            cursor?.use {
                val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val numCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                while (it.moveToNext()) {
                    val name = if (nameCol != -1) it.getString(nameCol) ?: "" else ""
                    val rawNum = if (numCol != -1) it.getString(numCol) ?: "" else ""
                    val photoUri = if (photoCol != -1) it.getString(photoCol) else null

                    val cleanNum = rawNum.replace(Regex("[\\s-()]"), "")
                    if (cleanNum.isBlank() || seenNumbers.contains(cleanNum)) {
                        continue
                    }

                    val matchesName = cleanQuery.isBlank() || name.lowercase().contains(cleanQuery)
                    val matchesNumber = digitsQuery.isNotBlank() && cleanNum.contains(digitsQuery)

                    if (cleanQuery.isBlank() || matchesName || matchesNumber) {
                        seenNumbers.add(cleanNum)
                        results.add(
                            ContactSuggestion(
                                name = name.ifBlank { rawNum },
                                number = rawNum,
                                photoUri = photoUri
                            )
                        )
                    }

                    // Limit results to 50 for smooth UI performance
                    if (cleanQuery.isBlank() && results.size >= 50) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        results
    }
}
