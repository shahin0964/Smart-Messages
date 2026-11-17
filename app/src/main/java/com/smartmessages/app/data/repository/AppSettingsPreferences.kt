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

    // ==========================================
    // 5. Dual SIM Settings
    // ==========================================
    // -1 = Always Ask, 0 = SIM 1, 1 = SIM 2
    var preferredSimSlot: Int
        get() = prefs.getInt("pref_preferred_sim_slot", -1)
        set(value) = prefs.edit().putInt("pref_preferred_sim_slot", value).apply()

    // ==========================================
    // 6. Messaging & Display Preferences
    // ==========================================
    var deliveryReports: Boolean
        get() = prefs.getBoolean("pref_delivery_reports", false)
        set(value) = prefs.edit().putBoolean("pref_delivery_reports", value).apply()

    var showCharCounter: Boolean
        get() = prefs.getBoolean("pref_show_char_counter", true)
        set(value) = prefs.edit().putBoolean("pref_show_char_counter", value).apply()

    var fontSize: String
        get() = prefs.getString("pref_font_size", "Normal") ?: "Normal"
        set(value) = prefs.edit().putString("pref_font_size", value).apply()

    var confirmDelete: Boolean
        get() = prefs.getBoolean("pref_confirm_delete", true)
        set(value) = prefs.edit().putBoolean("pref_confirm_delete", value).apply()

    var smsSignature: String
        get() = prefs.getString("pref_sms_signature", "") ?: ""
        set(value) = prefs.edit().putString("pref_sms_signature", value).apply()

    // ==========================================
    // 7. Notification Sound & Vibration
    // ==========================================
    var soundEnabled: Boolean
        get() = prefs.getBoolean("pref_sound_enabled", true)
        set(value) = prefs.edit().putBoolean("pref_sound_enabled", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("pref_vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("pref_vibration_enabled", value).apply()
}
