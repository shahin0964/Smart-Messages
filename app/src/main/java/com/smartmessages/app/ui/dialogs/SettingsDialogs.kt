package com.smartmessages.app.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartmessages.app.data.model.AppReleaseInfo
import com.smartmessages.app.data.model.UpdateStatus
import com.smartmessages.app.data.repository.AppSettingsPreferences
import com.smartmessages.app.data.repository.OtaUpdateManager
import com.smartmessages.app.data.repository.SmsBackupManager
import com.smartmessages.app.data.repository.SmsRepository
import com.smartmessages.app.ui.theme.BgLight
import com.smartmessages.app.ui.theme.PurplePrimary
import com.smartmessages.app.ui.theme.PurpleUltraLight
import com.smartmessages.app.ui.theme.TextDark
import com.smartmessages.app.ui.theme.TextMuted
import com.smartmessages.app.ui.theme.TransactionGreen
import kotlinx.coroutines.launch
import java.io.File

// ==========================================
// 1. SMART CATEGORIZATION DIALOG
// ==========================================
@Composable
fun SmartCategorizationDialog(
    preferences: AppSettingsPreferences,
    onDismiss: () -> Unit,
    onApplied: () -> Unit
) {
    var masterEnabled by remember { mutableStateOf(preferences.smartCategorizationEnabled) }
    var personalEnabled by remember { mutableStateOf(preferences.showPersonalCategory) }
    var otpEnabled by remember { mutableStateOf(preferences.showOtpCategory) }
    var txEnabled by remember { mutableStateOf(preferences.showTransactionsCategory) }
    var spamEnabled by remember { mutableStateOf(preferences.showSpamCategory) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurpleUltraLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = PurplePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Smart Categorization",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Automatically sorts incoming SMS into organized categories entirely on-device without uploading your private conversations.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Master Toggle Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (masterEnabled) PurpleUltraLight else Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Categorization", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Text(if (masterEnabled) "Active on home screen" else "All messages in single list", fontSize = 12.sp, color = TextMuted)
                        }
                        Switch(
                            checked = masterEnabled,
                            onCheckedChange = { masterEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PurplePrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("VISIBLE TABS ON HOME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))

                CategoryTabToggleItem("Personal", "Friends, family, direct contacts", personalEnabled, masterEnabled) { personalEnabled = it }
                CategoryTabToggleItem("OTPs & Codes", "Verification passwords & logins", otpEnabled, masterEnabled) { otpEnabled = it }
                CategoryTabToggleItem("Transactions", "Bank credits, debits & mobile wallets", txEnabled, masterEnabled) { txEnabled = it }
                CategoryTabToggleItem("Spam & Promos", "Filtered ads, marketing & spam", spamEnabled, masterEnabled) { spamEnabled = it }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            preferences.smartCategorizationEnabled = masterEnabled
                            preferences.showPersonalCategory = personalEnabled
                            preferences.showOtpCategory = otpEnabled
                            preferences.showTransactionsCategory = txEnabled
                            preferences.showSpamCategory = spamEnabled
                            onApplied()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save & Apply", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTabToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = if (enabled) TextDark else TextMuted)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PurplePrimary
            )
        )
    }
}

// ==========================================
// 2. BLOCKED NUMBERS & KEYWORDS DIALOG
// ==========================================
@Composable
fun BlockedManagerDialog(
    preferences: AppSettingsPreferences,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var newNumberInput by remember { mutableStateOf("") }
    var newKeywordInput by remember { mutableStateOf("") }
    var blockedNumbers by remember { mutableStateOf(preferences.blockedNumbers.toList()) }
    var blockedKeywords by remember { mutableStateOf(preferences.blockedKeywords.toList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFEF4444))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Blocked Filters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = PurplePrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PurplePrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Numbers (${blockedNumbers.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Keywords (${blockedKeywords.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // TAB 0: BLOCKED NUMBERS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newNumberInput,
                            onValueChange = { newNumberInput = it },
                            placeholder = { Text("Enter number or sender ID...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newNumberInput.isNotBlank()) {
                                    preferences.addBlockedNumber(newNumberInput)
                                    blockedNumbers = preferences.blockedNumbers.toList()
                                    newNumberInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Block", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (blockedNumbers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No blocked numbers yet", color = TextMuted, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(blockedNumbers) { number ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(number, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                                    IconButton(
                                        onClick = {
                                            preferences.removeBlockedNumber(number)
                                            blockedNumbers = preferences.blockedNumbers.toList()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Unblock", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: BLOCKED KEYWORDS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newKeywordInput,
                            onValueChange = { newKeywordInput = it },
                            placeholder = { Text("e.g. lottery, loan, casino...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newKeywordInput.isNotBlank()) {
                                    preferences.addBlockedKeyword(newKeywordInput)
                                    blockedKeywords = preferences.blockedKeywords.toList()
                                    newKeywordInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (blockedKeywords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No blocked keywords set", color = TextMuted, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(blockedKeywords) { keyword ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(keyword, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextDark)
                                    IconButton(
                                        onClick = {
                                            preferences.removeBlockedKeyword(keyword)
                                            blockedKeywords = preferences.blockedKeywords.toList()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 3. BACKUP & RESTORE DIALOG
// ==========================================
@Composable
fun BackupRestoreDialog(
    repository: SmsRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var exportResultFile by remember { mutableStateOf<File?>(null) }
    var statusMessage by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isRestoring = true
                statusMessage = "Reading and restoring messages..."
                try {
                    val messages = SmsBackupManager.parseBackupFile(context, uri)
                    if (messages.isEmpty()) {
                        statusMessage = "No valid messages found in selected file."
                    } else {
                        val count = repository.restoreMessages(messages)
                        statusMessage = "Successfully restored $count messages!"
                        Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    statusMessage = "Error restoring: ${e.message}"
                } finally {
                    isRestoring = false
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurpleUltraLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PurplePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Backup & Restore",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Export all messages to an encrypted/standard JSON file to save on Google Drive, SD Card, or transfer to another phone.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // EXPORT ACTION CARD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PurplePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Messages to JSON", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Generates a local backup file and opens Android share sheet to save anywhere.", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isExporting = true
                                    statusMessage = "Exporting messages..."
                                    try {
                                        val messages = repository.getAllMessagesForBackup()
                                        val file = SmsBackupManager.createBackupFile(context, messages)
                                        if (file != null) {
                                            exportResultFile = file
                                            statusMessage = "Backup ready: ${file.name} (${messages.size} messages)"
                                            SmsBackupManager.shareBackup(context, file)
                                        } else {
                                            statusMessage = "Failed to create backup file."
                                        }
                                    } catch (e: Exception) {
                                        statusMessage = "Export error: ${e.message}"
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            enabled = !isExporting && !isRestoring,
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Exporting...")
                            } else {
                                Text("Export Backup Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RESTORE ACTION CARD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TransactionGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore from JSON Backup", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Select a previously exported Smart Messages JSON backup file to restore.", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            enabled = !isExporting && !isRestoring,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PurplePrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restoring...")
                            } else {
                                Text("Pick Backup File", fontWeight = FontWeight.Bold, color = PurplePrimary)
                            }
                        }
                    }
                }

                if (statusMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(statusMessage, fontSize = 12.sp, color = PurplePrimary, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))
                ) {
                    Text("Close", color = TextDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 4. CHECK FOR UPDATES OTA DIALOG
// ==========================================
@Composable
fun CheckUpdateDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateStatus by remember { mutableStateOf<UpdateStatus>(UpdateStatus.Checking) }
    var currentVersion by remember { mutableStateOf(OtaUpdateManager.getCurrentVersion(context)) }

    LaunchedEffect(Unit) {
        updateStatus = UpdateStatus.Checking
        val result = OtaUpdateManager.checkForUpdates(context)
        updateStatus = result
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurpleUltraLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = PurplePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Check for Updates", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Current: v$currentVersion", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val status = updateStatus) {
                    is UpdateStatus.Checking -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = PurplePrimary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Checking GitHub for newer releases...", fontSize = 14.sp, color = TextMuted)
                        }
                    }

                    is UpdateStatus.UpToDate -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TransactionGreen, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("You are on the latest version!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Smart Messages v$currentVersion is up to date.", fontSize = 13.sp, color = TextMuted)
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Got it", fontWeight = FontWeight.Bold)
                        }
                    }

                    is UpdateStatus.UpdateAvailable -> {
                        val release = status.latestRelease
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("New Version Available: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                                Text(release.tagName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PurplePrimary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = release.body.ifBlank { "Performance improvements and bug fixes." },
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        updateStatus = UpdateStatus.Downloading(0, 0, release.apkSize, release)
                                        val downloadResult = OtaUpdateManager.downloadApk(context, release) { percent, downloaded, total ->
                                            updateStatus = UpdateStatus.Downloading(percent, downloaded, total, release)
                                        }
                                        downloadResult.onSuccess { apkFile ->
                                            updateStatus = UpdateStatus.Downloaded(apkFile, release)
                                            OtaUpdateManager.installApk(context, apkFile)
                                        }.onFailure { err ->
                                            updateStatus = UpdateStatus.Error(err.message ?: "Download failed")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Text("Download & Install Update", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is UpdateStatus.Downloading -> {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                            Text("Downloading update: ${status.progressPercent}%", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = (status.progressPercent / 100f).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = PurplePrimary,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    is UpdateStatus.Downloaded -> {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("APK Downloaded Successfully!", fontWeight = FontWeight.Bold, color = TransactionGreen)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { OtaUpdateManager.installApk(context, status.apkFile) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Text("Open Installer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is UpdateStatus.Error -> {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(status.message, color = Color(0xFFEF4444), fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        updateStatus = UpdateStatus.Checking
                                        updateStatus = OtaUpdateManager.checkForUpdates(context)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

// ==========================================
// 5. ABOUT APP DIALOG
// ==========================================
@Composable
fun AboutAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentVersion = OtaUpdateManager.getCurrentVersion(context)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gradient App Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Smart Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("Version $currentVersion (Build Release)", fontSize = 12.sp, color = PurplePrimary, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "A private, intelligent Android messaging application designed for Bangladesh and global users with automatic smart categorization, 1-tap OTP copy, and offline spam shielding.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleUltraLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("• 100% On-Device Processing (No Cloud)", fontSize = 12.sp, color = TextDark)
                        Text("• Dual SIM & Bangla Font Support", fontSize = 12.sp, color = TextDark)
                        Text("• bKash, Nagad, Bank SMS Parser", fontSize = 12.sp, color = TextDark)
                        Text("• Open-Source & Ad-Free", fontSize = 12.sp, color = TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shahin0964/Smart-Messages")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GitHub", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
