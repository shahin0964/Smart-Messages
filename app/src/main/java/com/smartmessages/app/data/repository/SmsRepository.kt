package com.smartmessages.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import com.smartmessages.app.data.model.Conversation
import com.smartmessages.app.data.model.Message
import com.smartmessages.app.data.model.MessageCategory
import com.smartmessages.app.util.OtpExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SmsRepository(private val context: Context) {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val contactCache = mutableMapOf<String, String?>()

    /**
     * Loads all conversation threads from the Telephony ContentProvider.
     */
    suspend fun loadConversations(): List<Conversation> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val result = mutableListOf<Conversation>()

        val projection = arrayOf(
            Telephony.Sms.Conversations.THREAD_ID,
            Telephony.Sms.Conversations.SNIPPET,
            Telephony.Sms.Conversations.MESSAGE_COUNT
        )

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Conversations.CONTENT_URI,
                projection,
                null,
                null,
                "date DESC"
            )

            cursor?.use {
                val threadIdCol = it.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID)
                val snippetCol = it.getColumnIndex(Telephony.Sms.Conversations.SNIPPET)

                while (it.moveToNext()) {
                    val threadId = it.getLong(threadIdCol)
                    val snippet = if (snippetCol != -1) it.getString(snippetCol) ?: "" else ""

                    // Fetch latest message metadata for address and date
                    val (address, date, unread) = getThreadDetails(threadId)
                    if (address.isNotBlank()) {
                        val contactName = getContactName(address)
                        val category = CategoryClassifier.classify(address, snippet)
                        val latestOtp = OtpExtractor.extractOtp(snippet)

                        result.add(
                            Conversation(
                                threadId = threadId,
                                address = address,
                                contactName = contactName,
                                snippet = snippet,
                                date = date,
                                unreadCount = unread,
                                category = category,
                                latestOtp = latestOtp
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }

        _conversations.value = result
        result
    }

    private fun getThreadDetails(threadId: Long): Triple<String, Long, Int> {
        var address = ""
        var date = 0L
        var unread = 0

        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.DATE,
            Telephony.Sms.READ
        )

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressCol = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val dateCol = it.getColumnIndex(Telephony.Sms.DATE)
                val readCol = it.getColumnIndex(Telephony.Sms.READ)

                var isFirst = true
                while (it.moveToNext()) {
                    if (isFirst) {
                        address = it.getString(addressCol) ?: ""
                        date = it.getLong(dateCol)
                        isFirst = false
                    }
                    val isRead = it.getInt(readCol) == 1
                    if (!isRead) {
                        unread++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Triple(address, date, unread)
    }

    /**
     * Loads all messages for a specific conversation thread.
     */
    suspend fun getMessagesForThread(threadId: Long): List<Message> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<Message>()

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ,
            Telephony.Sms.STATUS
        )

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndex(Telephony.Sms._ID)
                val addressCol = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyCol = it.getColumnIndex(Telephony.Sms.BODY)
                val dateCol = it.getColumnIndex(Telephony.Sms.DATE)
                val typeCol = it.getColumnIndex(Telephony.Sms.TYPE)
                val readCol = it.getColumnIndex(Telephony.Sms.READ)
                val statusCol = it.getColumnIndex(Telephony.Sms.STATUS)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val address = it.getString(addressCol) ?: ""
                    val body = it.getString(bodyCol) ?: ""
                    val date = it.getLong(dateCol)
                    val type = it.getInt(typeCol)
                    val isIncoming = (type == Telephony.Sms.MESSAGE_TYPE_INBOX)
                    val isRead = (it.getInt(readCol) == 1)
                    val status = it.getInt(statusCol)
                    val otp = OtpExtractor.extractOtp(body)

                    messages.add(
                        Message(
                            id = id,
                            threadId = threadId,
                            address = address,
                            body = body,
                            date = date,
                            isIncoming = isIncoming,
                            isRead = isRead,
                            status = status,
                            otpCode = otp
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        messages
    }

    /**
     * Resolves contact display name from phone address.
     */
    fun getContactName(phoneNumber: String): String? {
        if (contactCache.containsKey(phoneNumber)) {
            return contactCache[phoneNumber]
        }

        var contactName: String? = null
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameCol = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameCol != -1) {
                        contactName = it.getString(nameCol)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        contactCache[phoneNumber] = contactName
        return contactName
    }

    /**
     * Sends an SMS message and writes it to Telephony.Sms.Sent table.
     */
    suspend fun sendSms(address: String, body: String, subId: Int? = null): Boolean = withContext(Dispatchers.IO) {
        if (address.isBlank() || body.isBlank()) return@withContext false

        try {
            val smsManager: SmsManager = if (subId != null && subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            }

            val parts = smsManager.divideMessage(body)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(address, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(address, null, body, null, null)
            }

            // Save sent SMS to Telephony Provider
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            }
            context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)

            loadConversations()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Marks all messages in a thread as read.
     */
    suspend fun markThreadAsRead(threadId: Long) = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.SEEN, 1)
            }
            context.contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0",
                arrayOf(threadId.toString())
            )
            loadConversations()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Marks all unread messages across all threads as read.
     */
    suspend fun markAllAsRead(): Boolean = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.SEEN, 1)
            }
            val updated = context.contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms.READ} = 0",
                null
            )
            loadConversations()
            updated >= 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes a conversation thread.
     */
    suspend fun deleteThread(threadId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val deleted = context.contentResolver.delete(
                Telephony.Sms.CONTENT_URI,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString())
            )
            loadConversations()
            deleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
