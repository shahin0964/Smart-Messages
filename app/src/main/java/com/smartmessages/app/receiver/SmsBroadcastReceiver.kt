package com.smartmessages.app.receiver

import android.content.Context
import android.content.Intent

/**
 * Receiver for android.provider.Telephony.SMS_RECEIVED broadcast.
 * Handles incoming SMS when the app is running in standard mode or as a fallback.
 */
class SmsBroadcastReceiver : SmsReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }
}
