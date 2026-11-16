package com.smartmessages.app.data.repository

import android.content.Context
import android.content.SharedPreferences

class AppSettingsPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_messages_preferences", Context.MODE_PRIVATE)

    // ==========================================
    // 1. Smart Features & OTP Settings
    // ==========================================
    var autoCopyOtp: Boolean
        get() = prefs.getBoolean("pref_auto_copy_otp", true)
        set(value) = prefs.edit().putBoolean("pref_auto_copy_otp", value).apply()

    var priorityOtpAlerts: Boolean
        get() = prefs.getBoolean("pref_priority_otp_alerts", true)
        set(value) = prefs.edit().putBoolean("pref_priority_otp_alerts", value).apply()

    // ==========================================
    // 2. Smart Categorization
    // ==========================================
    var smartCategorizationEnabled: Boolean
        get() = prefs.getBoolean("pref_smart_categorization", true)
        set(value) = prefs.edit().putBoolean("pref_smart_categorization", value).apply()

    var showPersonalCategory: Boolean
        get() = prefs.getBoolean("pref_cat_personal", true)
        set(value) = prefs.edit().putBoolean("pref_cat_personal", value).apply()

    var showOtpCategory: Boolean
        get() = prefs.getBoolean("pref_cat_otp", true)
        set(value) = prefs.edit().putBoolean("pref_cat_otp", value).apply()

    var showTransactionsCategory: Boolean
        get() = prefs.getBoolean("pref_cat_transactions", true)
        set(value) = prefs.edit().putBoolean("pref_cat_transactions", value).apply()

    var showSpamCategory: Boolean
        get() = prefs.getBoolean("pref_cat_spam", true)
        set(value) = prefs.edit().putBoolean("pref_cat_spam", value).apply()

    // ==========================================
    // 3. Spam & Security (Scam & Phishing Shield)
    // ==========================================
    var scamShieldEnabled: Boolean
        get() = prefs.getBoolean("pref_scam_shield", true)
        set(value) = prefs.edit().putBoolean("pref_scam_shield", value).apply()

    var blockedNumbers: Set<String>
        get() = prefs.getStringSet("pref_blocked_numbers", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("pref_blocked_numbers", value).apply()

    var blockedKeywords: Set<String>
        get() = prefs.getStringSet("pref_blocked_keywords", setOf(
            "lottery", "winner", "jackpot", "1xbet", "melbet", "casino", "free bonus"
        )) ?: emptySet()
        set(value) = prefs.edit().putStringSet("pref_blocked_keywords", value).apply()

    fun addBlockedNumber(number: String) {
        val clean = number.trim()
        if (clean.isNotBlank()) {
            val updated = blockedNumbers.toMutableSet().apply { add(clean) }
            blockedNumbers = updated
        }
    }

    fun removeBlockedNumber(number: String) {
        val clean = number.trim()
        val updated = blockedNumbers.toMutableSet().apply { remove(clean) }
        blockedNumbers = updated
    }

    fun isNumberBlocked(address: String): Boolean {
        val clean = address.replace(Regex("[^0-9+]"), "")
        return blockedNumbers.any { blocked ->
            val cleanBlocked = blocked.replace(Regex("[^0-9+]"), "")
            clean.endsWith(cleanBlocked) || cleanBlocked.endsWith(clean) || address.equals(blocked, ignoreCase = true)
        }
    }

    fun addBlockedKeyword(keyword: String) {
        val clean = keyword.trim().lowercase()
        if (clean.isNotBlank()) {
            val updated = blockedKeywords.toMutableSet().apply { add(clean) }
            blockedKeywords = updated
        }
    }

    fun removeBlockedKeyword(keyword: String) {
        val clean = keyword.trim().lowercase()
        val updated = blockedKeywords.toMutableSet().apply { remove(clean) }
        blockedKeywords = updated
    }

    fun containsBlockedKeyword(body: String): Boolean {
        val lower = body.lowercase()
        return blockedKeywords.any { lower.contains(it.lowercase()) }
    }

    // ==========================================
    // 4. Storage & Backup Settings
    // ==========================================
    var autoDeleteExpiredOtp: Boolean
        get() = prefs.getBoolean("pref_auto_delete_expired_otp", false)
        set(value) = prefs.edit().putBoolean("pref_auto_delete_expired_otp", value).apply()
}
