package com.smartmessages.app.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat

object PermissionHelper {

    val REQUIRED_PERMISSIONS = buildList {
        add(android.Manifest.permission.READ_SMS)
        add(android.Manifest.permission.SEND_SMS)
        add(android.Manifest.permission.RECEIVE_SMS)
        add(android.Manifest.permission.READ_CONTACTS)
        add(android.Manifest.permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasRequiredPermissions(context: Context): Boolean {
        return hasSmsPermission(context)
    }

    fun isDefaultSmsApp(context: Context): Boolean {
        return try {
            val defaultPackage = Telephony.Sms.getDefaultSmsPackage(context)
            context.packageName.equals(defaultPackage, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    fun requestDefaultSmsApp(activity: Activity, requestCode: Int = 1001) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = activity.getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    activity.startActivityForResult(intent, requestCode)
                }
            } else {
                @Suppress("DEPRECATION")
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
                }
                activity.startActivityForResult(intent, requestCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
