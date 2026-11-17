package com.smartmessages.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SmsEvents {
    data class SmsReceivedEvent(
        val address: String,
        val body: String,
        val timestamp: Long
    )

    private val _newSmsFlow = MutableSharedFlow<SmsReceivedEvent>(extraBufferCapacity = 64)
    val newSmsFlow = _newSmsFlow.asSharedFlow()

    fun emitNewSms(address: String, body: String, timestamp: Long) {
        _newSmsFlow.tryEmit(SmsReceivedEvent(address, body, timestamp))
    }
}
