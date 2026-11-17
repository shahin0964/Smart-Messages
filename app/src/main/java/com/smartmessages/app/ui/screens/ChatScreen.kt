package com.smartmessages.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmessages.app.data.model.Message
import com.smartmessages.app.data.repository.AppSettingsPreferences
import com.smartmessages.app.data.repository.SimRepository
import com.smartmessages.app.data.repository.SmsRepository
import com.smartmessages.app.ui.components.MessageBubble
import com.smartmessages.app.util.SmsEvents
import com.smartmessages.app.ui.theme.BgLight
import com.smartmessages.app.ui.theme.DividerColor
import com.smartmessages.app.ui.theme.OtpAmber
import com.smartmessages.app.ui.theme.OtpAmberContainer
import com.smartmessages.app.ui.theme.PurplePrimary
import com.smartmessages.app.ui.theme.PurpleUltraLight
import com.smartmessages.app.ui.theme.TextDark
import com.smartmessages.app.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    threadId: Long,
    address: String,
    repository: SmsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AppSettingsPreferences(context) }
    val simRepo = remember { SimRepository(context) }

    val simList = remember { simRepo.getSimCards() }
    val preferredSlot = preferences.preferredSimSlot
    var selectedSim by remember(simList, preferredSlot) {
        mutableStateOf(
            if (preferredSlot in 0..1) {
                simList.firstOrNull { it.slotIndex == preferredSlot } ?: simList.firstOrNull()
            } else {
                simList.firstOrNull()
            }
        )
    }

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val contactName = remember(address) { repository.getContactName(address) }
    val displayName = contactName ?: address

    val listState = rememberLazyListState()

    val reloadMessages: () -> Unit = {
        scope.launch {
            try {
                var list = if (threadId > 0) repository.getMessagesForThread(threadId) else emptyList()
                if (list.isEmpty() && address.isNotBlank()) {
                    list = repository.getMessagesByAddress(address)
                }
                messages = list
                if (threadId > 0) {
                    repository.markThreadAsRead(threadId)
                }
                if (list.isNotEmpty()) {
                    try {
                        listState.scrollToItem(list.size - 1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(threadId) {
        reloadMessages()
    }

    // Real-time listener for incoming SMS from this sender or thread
    LaunchedEffect(threadId, address) {
        SmsEvents.newSmsFlow.collect { event ->
            val cleanIncoming = event.address.replace(Regex("[^0-9+]"), "")
            val cleanCurrent = address.replace(Regex("[^0-9+]"), "")
            if (cleanIncoming.endsWith(cleanCurrent) || cleanCurrent.endsWith(cleanIncoming) ||
                event.address.equals(address, ignoreCase = true)) {
                reloadMessages()
            }
        }
    }

    // Live ContentObserver on Telephony.Sms so external/system inserts refresh chat immediately
    DisposableEffect(threadId) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                reloadMessages()
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val latestOtp = remember(messages) {
        messages.lastOrNull { it.isIncoming }?.otpCode
    }

    val fontSizeSp = remember(preferences.fontSize) {
        when (preferences.fontSize) {
            "Small" -> 13f
            "Large" -> 18f
            else -> 15f
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PurpleUltraLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                                    color = PurplePrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    maxLines = 1
                                )
                                if (contactName != null) {
                                    Text(
                                        text = address,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Contact",
                                tint = PurplePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 0.8.dp)
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Character Counter
                    if (preferences.showCharCounter && inputText.isNotEmpty()) {
                        val isUnicode = inputText.any { it.code > 127 }
                        val singleLimit = if (isUnicode) 70 else 160
                        val multiLimit = if (isUnicode) 67 else 153
                        val charCount = inputText.length
                        val msgCount = if (charCount <= singleLimit) 1 else ((charCount + multiLimit - 1) / multiLimit)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (msgCount > 1) "$charCount / $singleLimit ($msgCount SMS)" else "$charCount / $singleLimit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (charCount > singleLimit) PurplePrimary else TextMuted
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SIM Selector chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PurpleUltraLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (simList.isNotEmpty()) {
                                        val currentIndex = simList.indexOf(selectedSim)
                                        val nextIndex = (currentIndex + 1) % simList.size
                                        selectedSim = simList[nextIndex]
                                        Toast.makeText(
                                            context,
                                            "Switched to ${selectedSim?.shortLabel} (${selectedSim?.carrierName})",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SimCard,
                                    contentDescription = "Switch SIM",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedSim?.shortLabel ?: "SIM 1",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Text message...", color = TextMuted) },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = DividerColor
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val canSend = inputText.isNotBlank() && !isSending
                        IconButton(
                            onClick = {
                                val textToSend = inputText.trim()
                                if (textToSend.isNotEmpty() && !isSending) {
                                    isSending = true
                                    scope.launch {
                                        val finalBody = if (preferences.smsSignature.isNotBlank()) {
                                            "$textToSend\n${preferences.smsSignature.trim()}"
                                        } else {
                                            textToSend
                                        }
                                        val success = repository.sendSms(
                                            address = address,
                                            body = finalBody,
                                            subId = selectedSim?.subscriptionId
                                        )
                                        if (success) {
                                            inputText = ""
                                            reloadMessages()
                                        } else {
                                            Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                                        }
                                        isSending = false
                                    }
                                }
                            },
                            enabled = canSend,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (canSend) PurplePrimary else Color(0xFFE2E8F0),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else TextMuted
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgLight)
                .padding(paddingValues)
        ) {
            // Sticky OTP Banner at top if conversation has active OTP
            AnimatedVisibility(visible = latestOtp != null) {
                Surface(
                    color = OtpAmberContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = OtpAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verification Code",
                                fontSize = 12.sp,
                                color = OtpAmber
                            )
                            Text(
                                text = latestOtp ?: "",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = OtpAmber
                            )
                        }
                        ElevatedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("OTP Code", latestOtp)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "OTP $latestOtp copied!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = OtpAmber,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Copy Code", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Bubble list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                itemsIndexed(
                    items = messages,
                    key = { index, message -> "${message.id}_${message.date}_$index" }
                ) { _, message ->
                    MessageBubble(
                        message = message,
                        fontSizeSp = fontSizeSp
                    )
                }
            }
        }
    }
}
