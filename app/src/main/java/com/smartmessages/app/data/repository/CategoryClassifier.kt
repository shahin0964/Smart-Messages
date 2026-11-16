package com.smartmessages.app.data.repository

import com.smartmessages.app.data.model.MessageCategory
import com.smartmessages.app.util.OtpExtractor

object CategoryClassifier {

    private val TRANSACTION_KEYWORDS = listOf(
        "credited", "debited", "transferred", "trxid", "txn", "transaction",
        "bkash", "nagad", "rocket", "upay", "cellfin", "bank", "atm",
        "balance", "acct", "a/c", "fee", "payment", "received tk", "sent tk",
        "cash in", "cash out", "recharge", "invoice", "statement", "bill"
    )

    private val SPAM_KEYWORDS = listOf(
        "win cash", "lottery", "congratulations you won", "jackpot",
        "betting", "casino", "1xbet", "melbet", "parimatch", "lucky winner",
        "claim your reward", "free money", "earn from home", "subscribe now for only",
        "click here to claim", "urgent notice: your account will be suspended",
        "free spins", "exclusive bonus", "click link", "loan approved without doc"
    )

    private val SCAM_PATTERNS = listOf(
        "bit.ly/", "tinyurl.com/", "t.co/", "goo.gl/", "is.gd/",
        "verify your bank", "kyc suspended", "sim block", "pin share",
        "bkash agent offer", "lottery winner"
    )

    fun classify(
        address: String,
        body: String,
        preferences: AppSettingsPreferences? = null
    ): MessageCategory {
        val lowerBody = body.lowercase()
        val lowerAddress = address.lowercase()

        // If user disabled Smart Categorization entirely, keep in Personal
        if (preferences != null && !preferences.smartCategorizationEnabled) {
            return MessageCategory.PERSONAL
        }

        // 1. Check user blocked numbers & keywords
        if (preferences != null) {
            if (preferences.isNumberBlocked(address)) {
                return MessageCategory.SPAM
            }
            if (preferences.containsBlockedKeyword(body)) {
                return MessageCategory.SPAM
            }
        }

        // 2. Check Scam & Phishing Shield
        val isScamShieldActive = preferences?.scamShieldEnabled ?: true
        if (isScamShieldActive) {
            if (SCAM_PATTERNS.any { lowerBody.contains(it) } || SPAM_KEYWORDS.any { lowerBody.contains(it) }) {
                return MessageCategory.SPAM
            }
        }

        // 3. Check for OTP
        val otp = OtpExtractor.extractOtp(body)
        if (otp != null) {
            return MessageCategory.OTP
        }

        // 4. Check for Financial / Bank / Wallet Transactions
        val isSenderTransaction = listOf("bkash", "nagad", "rocket", "upay", "bank").any { lowerAddress.contains(it) }
        val hasTransactionText = TRANSACTION_KEYWORDS.any { lowerBody.contains(it) }
        if (isSenderTransaction || hasTransactionText) {
            return MessageCategory.TRANSACTIONS
        }

        // 5. Default to Personal
        return MessageCategory.PERSONAL
    }
}
