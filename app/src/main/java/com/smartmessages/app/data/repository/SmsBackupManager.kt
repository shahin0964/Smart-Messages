package com.smartmessages.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.smartmessages.app.data.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsBackupManager {

    /**
     * Exports a list of messages to a JSON backup file in cache/backup directory.
     */
    suspend fun createBackupFile(context: Context, messages: List<Message>): File? = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.cacheDir, "backup").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(backupDir, "SmartMessages_Backup_$timeStamp.json")

            val rootJson = JSONObject().apply {
                put("app", "Smart Messages")
                put("version", "1.0")
                put("exportDate", System.currentTimeMillis())
                put("messageCount", messages.size)

                val messageArray = JSONArray()
                messages.forEach { msg ->
                    val obj = JSONObject().apply {
                        put("id", msg.id)
                        put("address", msg.address)
                        put("body", msg.body)
                        put("date", msg.date)
                        put("isIncoming", msg.isIncoming)
                        put("isRead", msg.isRead)
                        put("status", msg.status)
                    }
                    messageArray.put(obj)
                }
                put("messages", messageArray)
            }

            file.writeText(rootJson.toString(2))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Opens Android Share Sheet to let the user save or send their backup JSON file.
     */
    fun shareBackup(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Smart Messages Backup (${file.name})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Share Backup"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Parses a JSON backup file from a content Uri and returns a list of restored Message objects.
     */
    suspend fun parseBackupFile(context: Context, uri: Uri): List<Message> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Message>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext emptyList()
            val content = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            val root = JSONObject(content)
            val messageArray = root.optJSONArray("messages") ?: JSONArray()

            for (i in 0 until messageArray.length()) {
                val obj = messageArray.getJSONObject(i)
                list.add(
                    Message(
                        id = obj.optLong("id"),
                        threadId = 0L,
                        address = obj.optString("address"),
                        body = obj.optString("body"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        isIncoming = obj.optBoolean("isIncoming", true),
                        isRead = obj.optBoolean("isRead", true),
                        status = obj.optInt("status", -1)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }
}
