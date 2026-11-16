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
        "click here to claim", "urgent notice: your account will be suspended"
    )

    fun classify(address: String, body: String): MessageCategory {
        val lowerBody = body.lowercase()
        val lowerAddress = address.lowercase()

        // 1. Check for Spam / Phishing
        if (SPAM_KEYWORDS.any { lowerBody.contains(it) }) {
            return MessageCategory.SPAM
        }

        // 2. Check for OTP
        val otp = OtpExtractor.extractOtp(body)
        if (otp != null) {
            return MessageCategory.OTP
        }

        // 3. Check for Financial / Bank / Wallet Transactions
        val isSenderTransaction = listOf("bkash", "nagad", "rocket", "upay", "bank").any { lowerAddress.contains(it) }
        val hasTransactionText = TRANSACTION_KEYWORDS.any { lowerBody.contains(it) }
        if (isSenderTransaction || hasTransactionText) {
            return MessageCategory.TRANSACTIONS
        }

        // 4. Default to Personal
        return MessageCategory.PERSONAL
    }
}
