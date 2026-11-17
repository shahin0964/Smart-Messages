package com.smartmessages.app.data.model

data class SimInfo(
    val subscriptionId: Int,
    val slotIndex: Int, // 0 for SIM 1, 1 for SIM 2
    val displayName: String,
    val carrierName: String
) {
    val shortLabel: String
        get() = "SIM ${slotIndex + 1}"
}
