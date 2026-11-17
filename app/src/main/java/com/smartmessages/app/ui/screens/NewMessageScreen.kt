package com.smartmessages.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmessages.app.data.model.SimInfo
import com.smartmessages.app.data.repository.AppSettingsPreferences
import com.smartmessages.app.data.repository.ContactSuggestion
import com.smartmessages.app.data.repository.ContactsRepository
import com.smartmessages.app.data.repository.SimRepository
import com.smartmessages.app.data.repository.SmsRepository
import com.smartmessages.app.ui.theme.BgLight
import com.smartmessages.app.ui.theme.DividerColor
import com.smartmessages.app.ui.theme.PurplePrimary
import com.smartmessages.app.ui.theme.PurpleUltraLight
import com.smartmessages.app.ui.theme.TextDark
import com.smartmessages.app.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    repository: SmsRepository,
    onBack: () -> Unit,
    onSent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AppSettingsPreferences(context) }
    val contactsRepo = remember { ContactsRepository(context) }
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

    var recipient by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    var suggestions by remember { mutableStateOf<List<ContactSuggestion>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(false) }

    LaunchedEffect(recipient) {
        isLoadingContacts = true
        suggestions = contactsRepo.getContactSuggestions(recipient)
        isLoadingContacts = false
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "New Conversation",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
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
                    // Character counter
                    if (preferences.showCharCounter && messageBody.isNotEmpty()) {
                        val isUnicode = messageBody.any { it.code > 127 }
                        val singleLimit = if (isUnicode) 70 else 160
                        val multiLimit = if (isUnicode) 67 else 153
                        val charCount = messageBody.length
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
                            value = messageBody,
                            onValueChange = { messageBody = it },
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

                        val canSend = recipient.isNotBlank() && messageBody.isNotBlank() && !isSending
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    isSending = true
                                    scope.launch {
                                        val finalBody = if (preferences.smsSignature.isNotBlank()) {
                                            "${messageBody.trim()}\n${preferences.smsSignature.trim()}"
                                        } else {
                                            messageBody.trim()
                                        }
                                        val success = repository.sendSms(
                                            address = recipient.trim(),
                                            body = finalBody,
                                            subId = selectedSim?.subscriptionId
                                        )
                                        if (success) {
                                            onSent(recipient.trim())
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Recipient Input (name or number)
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text("To", color = PurplePrimary) },
                placeholder = { Text("Type name or phone number", color = TextMuted) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PurplePrimary)
                },
                trailingIcon = {
                    if (recipient.isNotEmpty()) {
                        IconButton(onClick = { recipient = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = DividerColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Suggestions List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // If user entered something, allow direct sending to that number / input
                if (recipient.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    // Keep current recipient
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PurpleUltraLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PurplePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Send to $recipient",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PurplePrimary
                                    )
                                    Text(
                                        text = "Direct number",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                if (suggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = if (recipient.isBlank()) "CONTACTS" else "MATCHING CONTACTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    items(suggestions, key = { it.number + it.name }) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    recipient = contact.number
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PurpleUltraLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = contact.number,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                } else if (!isLoadingContacts && recipient.isBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Type a contact name or number above",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
