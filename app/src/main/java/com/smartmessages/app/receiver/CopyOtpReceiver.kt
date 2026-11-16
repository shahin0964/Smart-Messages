package com.smartmessages.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

class CopyOtpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val otp = intent.getStringExtra("EXTRA_OTP") ?: return
        val notifId = intent.getIntExtra("EXTRA_NOTIF_ID", -1)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("OTP Code", otp)
        clipboard?.setPrimaryClip(clip)

        Toast.makeText(context, "OTP $otp copied to clipboard!", Toast.LENGTH_SHORT).show()

        if (notifId != -1) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(notifId)
        }
    }
}
