package com.smartmessages.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Required Service for Android Default SMS App specification.
 * Handles RESPOND_VIA_MESSAGE intents (e.g. from Android Auto, Wearables, or quick replies).
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
