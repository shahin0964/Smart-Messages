package com.smartmessages.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Required BroadcastReceiver for Android Default SMS/MMS specification.
 * Handles WAP_PUSH_DELIVER_ACTION for incoming MMS.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Handled by telephony provider when default SMS app
    }
}
