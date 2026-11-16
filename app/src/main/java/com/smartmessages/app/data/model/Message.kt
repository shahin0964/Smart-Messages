package com.smartmessages.app.data.model

enum class MessageCategory(val displayName: String) {
    ALL("All"),
    PERSONAL("Personal"),
    OTP("OTPs"),
    TRANSACTIONS("Transactions"),
    SPAM("Spam")
}

data class Conversation(
    val threadId: Long,
    val address: String,
    val contactName: String?,
    val snippet: String,
    val date: Long,
    val unreadCount: Int,
    val category: MessageCategory = MessageCategory.PERSONAL,
    val isPinned: Boolean = false,
    val latestOtp: String? = null
)

data class Message(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val isIncoming: Boolean,
    val isRead: Boolean,
    val status: Int = -1,
    val subId: Int = -1,
    val otpCode: String? = null
)
