package com.smartmessages.app.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmessages.app.data.repository.AppSettingsPreferences
import com.smartmessages.app.data.repository.OtaUpdateManager
import com.smartmessages.app.data.repository.SmsRepository
import com.smartmessages.app.ui.dialogs.AboutAppDialog
import com.smartmessages.app.ui.dialogs.BackupRestoreDialog
import com.smartmessages.app.ui.dialogs.BlockedManagerDialog
import com.smartmessages.app.ui.dialogs.CheckUpdateDialog
import com.smartmessages.app.ui.dialogs.FontSizeDialog
import com.smartmessages.app.ui.dialogs.PreferredSimDialog
import com.smartmessages.app.ui.dialogs.SmartCategorizationDialog
import com.smartmessages.app.ui.dialogs.SmsSignatureDialog
import com.smartmessages.app.ui.theme.BgLight
import com.smartmessages.app.ui.theme.PurplePrimary
import com.smartmessages.app.ui.theme.PurpleUltraLight
import com.smartmessages.app.ui.theme.TextDark
import com.smartmessages.app.ui.theme.TextLightGray
import com.smartmessages.app.ui.theme.TextMuted
import com.smartmessages.app.ui.theme.TransactionGreen
import com.smartmessages.app.util.PermissionHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: SmsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AppSettingsPreferences(context) }

    var isDefaultSms by remember { mutableStateOf(PermissionHelper.isDefaultSmsApp(context)) }

    // Toggle States backed by SharedPreferences
    var autoCopyOtp by remember { mutableStateOf(preferences.autoCopyOtp) }
    var priorityOtpNotifs by remember { mutableStateOf(preferences.priorityOtpAlerts) }
    var spamProtectionEnabled by remember { mutableStateOf(preferences.scamShieldEnabled) }
    var autoDeleteExpiredOtp by remember { mutableStateOf(preferences.autoDeleteExpiredOtp) }

    // Dual SIM & Messaging States
    var preferredSimSlot by remember { mutableIntStateOf(preferences.preferredSimSlot) }
    var deliveryReports by remember { mutableStateOf(preferences.deliveryReports) }
    var showCharCounter by remember { mutableStateOf(preferences.showCharCounter) }
    var currentFontSize by remember { mutableStateOf(preferences.fontSize) }
    var confirmDelete by remember { mutableStateOf(preferences.confirmDelete) }
    var currentSignature by remember { mutableStateOf(preferences.smsSignature) }
    var soundEnabled by remember { mutableStateOf(preferences.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(preferences.vibrationEnabled) }

    // Dialog Visibility States
    var showCategorizationDialog by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPreferredSimDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showSignatureDialog by remember { mutableStateOf(false) }

    val currentVersion = remember { OtaUpdateManager.getCurrentVersion(context) }

    val preferredSimText = when (preferredSimSlot) {
        0 -> "SIM 1"
        1 -> "SIM 2"
        else -> "Always Ask"
    }

    val fontSizeText = when (currentFontSize) {
        "Small" -> "Small (13sp)"
        "Large" -> "Large (18sp)"
        else -> "Normal (15sp)"
    }

    val signatureText = if (currentSignature.isNotBlank()) currentSignature else "None"

    LaunchedEffect(Unit) {
        isDefaultSms = PermissionHelper.isDefaultSmsApp(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgLight)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Default SMS App Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleUltraLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                            Column {
                                Text(
                                    text = "Default SMS App",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = if (isDefaultSms) "Smart Messages is your default SMS app" else "Not set as default SMS app",
                                    fontSize = 12.sp,
                                    color = if (isDefaultSms) TransactionGreen else TextMuted
                                )
                            }
                        }
                    }

                    if (!isDefaultSms) {
                        Spacer(modifier = Modifier.height(12.dp))
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
                            Text("Set Smart Messages as Default", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SECTION 1: DUAL SIM & NETWORK
            SettingsSectionHeader("Dual SIM & Network")

            SettingsOptionItem(
                icon = Icons.Default.SimCard,
                title = "Preferred SIM for SMS",
                subtitle = preferredSimText,
                onClick = {
                    showPreferredSimDialog = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SECTION 2: MESSAGING PREFERENCES
            SettingsSectionHeader("Messaging Preferences")

            SettingsSwitchItem(
                icon = Icons.Default.DoneAll,
                title = "SMS Delivery Reports",
                subtitle = "Request delivery confirmation from mobile carrier",
                checked = deliveryReports,
                onCheckedChange = {
                    deliveryReports = it
                    preferences.deliveryReports = it
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Pin,
                title = "Character Counter",
                subtitle = "Show counter and SMS parts (160 / 70 characters)",
                checked = showCharCounter,
                onCheckedChange = {
                    showCharCounter = it
                    preferences.showCharCounter = it
                }
            )

            SettingsOptionItem(
                icon = Icons.Default.FormatSize,
                title = "Message Font Size",
                subtitle = fontSizeText,
                onClick = {
                    showFontSizeDialog = true
                }
            )

            SettingsOptionItem(
                icon = Icons.Default.EditNote,
                title = "SMS Signature",
                subtitle = signatureText,
                onClick = {
                    showSignatureDialog = true
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Delete,
                title = "Confirm Deletion",
                subtitle = "Ask for confirmation before deleting messages",
                checked = confirmDelete,
                onCheckedChange = {
                    confirmDelete = it
                    preferences.confirmDelete = it
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SECTION 3: NOTIFICATIONS & SOUNDS
            SettingsSectionHeader("Notifications & Sounds")

            SettingsSwitchItem(
                icon = Icons.Default.VolumeUp,
                title = "Message Sounds",
                subtitle = "Play sound for incoming text messages",
                checked = soundEnabled,
                onCheckedChange = {
                    soundEnabled = it
                    preferences.soundEnabled = it
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Vibration,
                title = "Vibrate on Message",
                subtitle = "Vibrate phone when a new message arrives",
                checked = vibrationEnabled,
                onCheckedChange = {
                    vibrationEnabled = it
                    preferences.vibrationEnabled = it
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SECTION 4: SMART FEATURES & OTP
            SettingsSectionHeader("Smart Features & OTP")

            SettingsSwitchItem(
                icon = Icons.Default.Key,
                title = "One-Tap Copy OTP",
                subtitle = "Detect and copy verification codes with one tap",
                checked = autoCopyOtp,
                onCheckedChange = {
                    autoCopyOtp = it
                    preferences.autoCopyOtp = it
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.NotificationsActive,
                title = "Priority OTP Alerts",
                subtitle = "High-priority heads-up banner with copy action",
                checked = priorityOtpNotifs,
                onCheckedChange = {
                    priorityOtpNotifs = it
                    preferences.priorityOtpAlerts = it
                }
            )

            SettingsOptionItem(
                icon = Icons.Default.Category,
                title = "Smart Categorization",
                subtitle = "Personal, OTPs, Transactions and Spam tabs",
                onClick = {
                    showCategorizationDialog = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SECTION 5: SPAM & SECURITY
            SettingsSectionHeader("Spam & Security")

            SettingsSwitchItem(
                icon = Icons.Default.Security,
                title = "Scam & Phishing Shield",
                subtitle = "Filter fraudulent messages and suspicious web links",
                checked = spamProtectionEnabled,
                onCheckedChange = {
                    spamProtectionEnabled = it
                    preferences.scamShieldEnabled = it
                    Toast.makeText(
                        context,
                        if (it) "Scam & Phishing Shield enabled" else "Scam Shield disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                    scope.launch { repository.loadConversations() }
                }
            )

            SettingsOptionItem(
                icon = Icons.Default.Block,
                title = "Blocked Numbers & Keywords",
                subtitle = "Manage blocked senders and custom filters",
                onClick = {
                    showBlockedDialog = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SECTION 6: STORAGE & BACKUP
            SettingsSectionHeader("Storage & Backup")

            SettingsSwitchItem(
                icon = Icons.Default.AutoDelete,
                title = "Auto-Delete Expired OTPs",
                subtitle = "Automatically clean up old verification codes after 24h",
                checked = autoDeleteExpiredOtp,
                onCheckedChange = {
                    autoDeleteExpiredOtp = it
                    preferences.autoDeleteExpiredOtp = it
                    if (it) {
                        scope.launch {
                            val deleted = repository.deleteExpiredOtps(24)
                            Toast.makeText(
                                context,
                                if (deleted > 0) "$deleted expired OTP messages deleted" else "No expired OTPs older than 24h found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )

            SettingsOptionItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore Messages",
                subtitle = "Export or restore messages to local JSON/XML file",
                onClick = {
                    showBackupDialog = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SECTION 7: ABOUT & UPDATES
            SettingsSectionHeader("About & Updates")

            SettingsOptionItem(
                icon = Icons.Default.SystemUpdate,
                title = "Check for Updates",
                subtitle = "Current Version: v$currentVersion",
                onClick = {
                    showUpdateDialog = true
                }
            )

            SettingsOptionItem(
                icon = Icons.Default.Info,
                title = "About Smart Messages",
                subtitle = "Private, modern, open-source Android messaging app",
                onClick = {
                    showAboutDialog = true
                }
            )
        }
    }

    // Interactive Dialogs
    if (showPreferredSimDialog) {
        PreferredSimDialog(
            preferences = preferences,
            onDismiss = { showPreferredSimDialog = false },
            onSelected = {
                preferredSimSlot = preferences.preferredSimSlot
            }
        )
    }

    if (showFontSizeDialog) {
        FontSizeDialog(
            preferences = preferences,
            onDismiss = { showFontSizeDialog = false },
            onSelected = {
                currentFontSize = preferences.fontSize
            }
        )
    }

    if (showSignatureDialog) {
        SmsSignatureDialog(
            preferences = preferences,
            onDismiss = { showSignatureDialog = false },
            onSaved = {
                currentSignature = preferences.smsSignature
            }
        )
    }

    if (showCategorizationDialog) {
        SmartCategorizationDialog(
            preferences = preferences,
            onDismiss = { showCategorizationDialog = false },
            onApplied = {
                scope.launch { repository.loadConversations() }
                Toast.makeText(context, "Categorization settings applied", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showBlockedDialog) {
        BlockedManagerDialog(
            preferences = preferences,
            onDismiss = {
                showBlockedDialog = false
                scope.launch { repository.loadConversations() }
            }
        )
    }

    if (showBackupDialog) {
        BackupRestoreDialog(
            repository = repository,
            onDismiss = { showBackupDialog = false }
        )
    }

    if (showUpdateDialog) {
        CheckUpdateDialog(
            onDismiss = { showUpdateDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PurpleUltraLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextLightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PurpleUltraLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PurplePrimary
                )
            )
        }
    }
}
