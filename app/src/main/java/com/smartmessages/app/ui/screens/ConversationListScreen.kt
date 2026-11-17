package com.smartmessages.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import com.smartmessages.app.data.repository.AppSettingsPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.smartmessages.app.data.model.MessageCategory
import com.smartmessages.app.data.repository.SmsRepository
import com.smartmessages.app.ui.components.ConversationItem
import com.smartmessages.app.ui.components.SmartCategoryChips
import com.smartmessages.app.ui.components.TopHeader
import com.smartmessages.app.ui.theme.BgLight
import com.smartmessages.app.ui.theme.DividerColor
import com.smartmessages.app.ui.theme.PurplePrimary
import com.smartmessages.app.ui.theme.PurpleUltraLight
import com.smartmessages.app.ui.theme.TextDark
import com.smartmessages.app.ui.theme.TextMuted
import com.smartmessages.app.util.PermissionHelper
import com.smartmessages.app.util.SmsEvents
import kotlinx.coroutines.launch

@Composable
fun ConversationListScreen(
    repository: SmsRepository,
    onOpenChat: (Long, String) -> Unit,
    onComposeNew: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val conversations by repository.conversations.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    val preferences = remember { AppSettingsPreferences(context) }
    var selectedCategory by remember { mutableStateOf(MessageCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var isDefaultSms by remember { mutableStateOf(PermissionHelper.isDefaultSmsApp(context)) }
    var hasSmsPerm by remember { mutableStateOf(PermissionHelper.hasSmsPermission(context)) }

    val visibleCategories = remember(preferences) {
        if (!preferences.smartCategorizationEnabled) {
            listOf(MessageCategory.ALL)
        } else {
            buildList {
                add(MessageCategory.ALL)
                if (preferences.showPersonalCategory) add(MessageCategory.PERSONAL)
                if (preferences.showOtpCategory) add(MessageCategory.OTP)
                if (preferences.showTransactionsCategory) add(MessageCategory.TRANSACTIONS)
                if (preferences.showSpamCategory) add(MessageCategory.SPAM)
            }
        }
    }

    LaunchedEffect(Unit) {
        hasSmsPerm = PermissionHelper.hasSmsPermission(context)
        if (hasSmsPerm) {
            repository.loadConversations()
            if (preferences.autoDeleteExpiredOtp && PermissionHelper.isDefaultSmsApp(context)) {
                repository.deleteExpiredOtps(24)
            }
        }
        isDefaultSms = PermissionHelper.isDefaultSmsApp(context)
    }

    // Real-time instant reload when incoming SMS arrives
    LaunchedEffect(Unit) {
        SmsEvents.newSmsFlow.collect {
            repository.loadConversations()
        }
    }

    // Filter conversations based on Category and Search Query
    val filteredList = remember(conversations, selectedCategory, searchQuery) {
        conversations.filter { conv ->
            val matchesCategory = (selectedCategory == MessageCategory.ALL || conv.category == selectedCategory)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.lowercase()
                (conv.contactName?.lowercase()?.contains(q) == true) ||
                        conv.address.lowercase().contains(q) ||
                        conv.snippet.lowercase().contains(q)
            }
            matchesCategory && matchesSearch
        }
    }

    // Compute category counts for badge indicators
    val categoryCounts = remember(conversations) {
        conversations.groupingBy { it.category }.eachCount()
    }

    Scaffold(
        topBar = {
            TopHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSearchActive = isSearchActive,
                onToggleSearch = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) searchQuery = ""
                },
                onSettingsClick = onOpenSettings,
                onMarkAllAsReadClick = {
                    if (!PermissionHelper.isDefaultSmsApp(context)) {
                        Toast.makeText(context, "Set Smart Messages as default SMS app to mark all as read", Toast.LENGTH_LONG).show()
                        (context as? Activity)?.let {
                            PermissionHelper.requestDefaultSmsApp(it)
                        }
                    } else {
                        scope.launch {
                            val success = repository.markAllAsRead()
                            if (success) {
                                Toast.makeText(context, "All messages marked as read", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to mark messages as read", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onSpamClick = {
                    selectedCategory = MessageCategory.SPAM
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onComposeNew,
                containerColor = PurplePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Start Chat")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgLight)
                .padding(paddingValues)
        ) {
            // SMS Permission Required Banner
            AnimatedVisibility(visible = !hasSmsPerm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleUltraLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SMS Permission Needed",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Allow SMS access to read and receive messages",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onRequestPermissions()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Allow Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Default SMS App Warning Banner if not set as default
            AnimatedVisibility(visible = !isDefaultSms) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleUltraLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Set as Default SMS App",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Enable smart OTP extraction and spam protection",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                (context as? Activity)?.let {
                                    PermissionHelper.requestDefaultSmsApp(it)
                                    isDefaultSms = PermissionHelper.isDefaultSmsApp(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set as Default", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Category Filter Chips Row (All, Personal, OTPs, Transactions, Spam)
            SmartCategoryChips(
                selectedCategory = selectedCategory,
                categoryCounts = categoryCounts,
                visibleCategories = visibleCategories,
                onSelectCategory = { selectedCategory = it }
            )

            HorizontalDivider(color = DividerColor, thickness = 0.8.dp)

            // Conversation List or Loading / Empty States
            if (isLoading && conversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No messages found" else "No conversations in ${selectedCategory.displayName}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = filteredList,
                        key = { index, item -> "${item.threadId}_${item.address}_$index" }
                    ) { _, conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = {
                                onOpenChat(conversation.threadId, conversation.address)
                            }
                        )
                        HorizontalDivider(
                            color = DividerColor,
                            thickness = 0.6.dp,
                            modifier = Modifier.padding(start = 78.dp)
                        )
                    }
                }
            }
        }
    }
}
