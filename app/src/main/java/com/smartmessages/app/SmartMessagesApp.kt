package com.smartmessages.app

import android.app.Application
import com.smartmessages.app.util.NotificationHelper

class SmartMessagesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
