package com.smartmessages.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.smartmessages.app.MainActivity
import com.smartmessages.app.R
import com.smartmessages.app.receiver.CopyOtpReceiver

object NotificationHelper {

    const val CHANNEL_SMS = "smart_messages_sms"
    const val CHANNEL_OTP = "smart_messages_otp"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val smsChannel = NotificationChannel(
                CHANNEL_SMS,
                "Incoming Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming personal and transactional SMS"
                enableVibration(true)
            }

            val otpChannel = NotificationChannel(
                CHANNEL_OTP,
                "OTP & Verification Codes",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Priority alerts with quick-copy for One-Time Passwords"
                enableVibration(true)
            }

            nm.createNotificationChannel(smsChannel)
            nm.createNotificationChannel(otpChannel)
        }
    }

    fun showSmsNotification(
        context: Context,
        sender: String,
        body: String,
        otpCode: String? = null,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val notifId = (System.currentTimeMillis() % 100000).toInt()

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isOtp = otpCode != null
        val channelId = if (isOtp) CHANNEL_OTP else CHANNEL_SMS

        val title = if (isOtp) "🔑 OTP: $otpCode • $sender" else sender

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (isOtp) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)

        if (!soundEnabled) {
            builder.setSilent(true)
        }
        if (!vibrationEnabled) {
            builder.setVibrate(longArrayOf(0))
        }

        // Add "Copy OTP" Action button if OTP was detected!
        if (isOtp) {
            val copyIntent = Intent(context, CopyOtpReceiver::class.java).apply {
                putExtra("EXTRA_OTP", otpCode)
                putExtra("EXTRA_NOTIF_ID", notifId)
            }
            val copyPendingIntent = PendingIntent.getBroadcast(
                context,
                notifId + 1,
                copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                android.R.drawable.ic_menu_save,
                "Copy $otpCode",
                copyPendingIntent
            )
        }

        try {
            nm.notify(notifId, builder.build())
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
