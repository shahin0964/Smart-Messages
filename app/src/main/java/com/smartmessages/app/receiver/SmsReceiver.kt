package com.smartmessages.app.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.smartmessages.app.data.repository.AppSettingsPreferences
import com.smartmessages.app.util.NotificationHelper
import com.smartmessages.app.util.OtpExtractor
import com.smartmessages.app.util.SmsEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections

open class SmsReceiver : BroadcastReceiver() {

    companion object {
        // Cache to deduplicate SMS broadcasts received via multiple actions
        private val recentMessageCache = Collections.synchronizedSet(LinkedHashSet<String>())

        fun processIncomingSms(context: Context, intent: Intent) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val address = messages[0].displayOriginatingAddress ?: ""
            val bodyBuilder = java.lang.StringBuilder()
            var timestamp = System.currentTimeMillis()

            for (sms in messages) {
                bodyBuilder.append(sms.displayMessageBody)
                timestamp = sms.timestampMillis
            }
            val fullBody = bodyBuilder.toString()

            // Deduplication key
            val dedupeKey = "$address|$timestamp|${fullBody.take(40)}"
            if (recentMessageCache.contains(dedupeKey)) {
                return
            }
            recentMessageCache.add(dedupeKey)
            if (recentMessageCache.size > 50) {
                val iterator = recentMessageCache.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }

            val preferences = AppSettingsPreferences(context)

            // If this app is the Default SMS app, write the received SMS into Telephony.Sms.Inbox immediately
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)
            val isDefaultApp = (context.packageName == defaultSmsPackage)

            if (isDefaultApp) {
                try {
                    // Resolve or create thread ID immediately so the message is indexed right away
                    val threadId = Telephony.Threads.getOrCreateThreadId(context, address)
                    val values = ContentValues().apply {
                        put(Telephony.Sms.THREAD_ID, threadId)
                        put(Telephony.Sms.ADDRESS, address)
                        put(Telephony.Sms.BODY, fullBody)
                        put(Telephony.Sms.DATE, timestamp)
                        put(Telephony.Sms.READ, 0)
                        put(Telephony.Sms.SEEN, 0)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                    }
                    context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Immediately emit event to active UI screens for instant real-time live update
            SmsEvents.emitNewSms(address, fullBody, timestamp)

            // Check if sender or keyword is blocked
            val isBlocked = preferences.isNumberBlocked(address) || preferences.containsBlockedKeyword(fullBody)
            if (isBlocked) {
                return
            }

            // Detect OTP
            val otpCode = OtpExtractor.extractOtp(fullBody)

            // Show Heads-Up Notification
            NotificationHelper.showSmsNotification(
                context = context,
                sender = address,
                body = fullBody,
                otpCode = otpCode,
                soundEnabled = preferences.soundEnabled,
                vibrationEnabled = preferences.vibrationEnabled
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Telephony.Sms.Intents.SMS_DELIVER_ACTION &&
            action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                processIncomingSms(context, intent)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
