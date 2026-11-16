package com.smartmessages.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmessages.app.data.model.Message
import com.smartmessages.app.ui.theme.OtpAmber
import com.smartmessages.app.ui.theme.OtpAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isIncoming = message.isIncoming

    val bubbleShape = if (isIncoming) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomEnd = 4.dp,
            bottomStart = 16.dp
        )
    }

    val bubbleColor = if (isIncoming) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }

    val textColor = if (isIncoming) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color.White
    }

    val alignment = if (isIncoming) Alignment.Start else Alignment.End

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Message text
                Text(
                    text = message.body,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 20.sp
                )

                // Inline OTP One-Tap Copy button if OTP detected!
                if (message.otpCode != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("OTP Code", message.otpCode)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "OTP ${message.otpCode} copied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = OtpAmberContainer,
                            contentColor = OtpAmber
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = OtpAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy OTP: ${message.otpCode}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.date))
                Text(
                    text = timeString,
                    color = if (isIncoming) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
