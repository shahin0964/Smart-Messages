package com.smartmessages.app.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.smartmessages.app.data.model.SimInfo

class SimRepository(private val context: Context) {

    fun getSimCards(): List<SimInfo> {
        val simList = mutableListOf<SimInfo>()

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val activeList: List<SubscriptionInfo>? = subscriptionManager?.activeSubscriptionInfoList

                if (!activeList.isNullOrEmpty()) {
                    activeList.forEach { info ->
                        val slot = info.simSlotIndex
                        val name = info.displayName?.toString()?.takeIf { it.isNotBlank() } ?: "SIM ${slot + 1}"
                        val carrier = info.carrierName?.toString()?.takeIf { it.isNotBlank() } ?: name
                        simList.add(
                            SimInfo(
                                subscriptionId = info.subscriptionId,
                                slotIndex = slot,
                                displayName = name,
                                carrierName = carrier
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If no SIMs found or permission not granted, provide fallback entries
        if (simList.isEmpty()) {
            simList.add(SimInfo(subscriptionId = 1, slotIndex = 0, displayName = "SIM 1", carrierName = "SIM 1"))
        }

        return simList.sortedBy { it.slotIndex }
    }
}
