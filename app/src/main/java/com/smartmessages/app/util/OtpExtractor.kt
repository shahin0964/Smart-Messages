package com.smartmessages.app.util

import java.util.regex.Pattern

object OtpExtractor {

    // Common keywords accompanying OTPs
    private val OTP_KEYWORDS = listOf(
        "otp", "one-time", "one time", "verification code", "verification",
        "security code", "auth code", "login code", "pin", "passcode",
        "secret code", "confirmation code", "validation code", "reset code"
    )

    // Regex to match 4 to 8 digit numbers often preceded or followed by keywords
    private val OTP_PATTERNS = listOf(
        Pattern.compile("""(?i)(?:code|otp|pin|passcode|secret)[\s:=-]+([0-9]{4,8})\b"""),
        Pattern.compile("""\b([0-9]{4,8})\b[\s]*(?:is your|is the|to verify|for verification|code)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?i)(?:use|enter)[\s]+([0-9]{4,8})""")
    )

    /**
     * Extracts an OTP / verification code from SMS text body if present.
     */
    fun extractOtp(body: String): String? {
        if (body.isBlank()) return null
        val lower = body.lowercase()

        // Check if message mentions OTP-related keywords
        val hasOtpKeyword = OTP_KEYWORDS.any { lower.contains(it) }
        if (!hasOtpKeyword) return null

        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (candidate != null && isValidOtp(candidate)) {
                    return candidate
                }
            }
        }

        // Fallback: look for 4-8 isolated digits if message has strong OTP keywords
        val isolatedPattern = Pattern.compile("""\b([0-9]{4,8})\b""")
        val isolatedMatcher = isolatedPattern.matcher(body)
        while (isolatedMatcher.find()) {
            val candidate = isolatedMatcher.group(1)
            if (candidate != null && isValidOtp(candidate)) {
                return candidate
            }
        }

        return null
    }

    private fun isValidOtp(candidate: String): Boolean {
        // Disallow year patterns like 2024, 2025, 2026
        if (candidate.length == 4 && (candidate.startsWith("19") || candidate.startsWith("20"))) {
            return false
        }
        // Disallow repeated identical digits like 0000, 111111
        if (candidate.all { it == candidate[0] }) {
            return false
        }
        return true
    }
}
