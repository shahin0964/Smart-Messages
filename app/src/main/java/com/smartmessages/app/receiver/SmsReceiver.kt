package com.smartmessages.app.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.smartmessages.app.util.NotificationHelper
import com.smartmessages.app.util.OtpExtractor

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Telephony.Sms.Intents.SMS_DELIVER_ACTION &&
            action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

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

        // If this app is the Default SMS app, write the received SMS into Telephony.Sms.Inbox
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)
        val isDefaultApp = (context.packageName == defaultSmsPackage)

        if (isDefaultApp) {
            try {
                val values = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, address)
                    put(Telephony.Sms.BODY, fullBody)
                    put(Telephony.Sms.DATE, timestamp)
                    put(Telephony.Sms.READ, 0)
                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                }
                context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Detect OTP
        val otpCode = OtpExtractor.extractOtp(fullBody)

        // Show Heads-Up Notification
        NotificationHelper.showSmsNotification(
            context = context,
            sender = address,
            body = fullBody,
            otpCode = otpCode
        )
    }
}
