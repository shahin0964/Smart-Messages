package com.smartmessages.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmessages.app.data.model.MessageCategory
import com.smartmessages.app.ui.theme.DividerColor
import com.smartmessages.app.ui.theme.OtpAmber
import com.smartmessages.app.ui.theme.PurplePrimary
import com.smartmessages.app.ui.theme.SpamRed
import com.smartmessages.app.ui.theme.TextDark
import com.smartmessages.app.ui.theme.TextMuted
import com.smartmessages.app.ui.theme.TransactionGreen

@Composable
fun SmartCategoryChips(
    selectedCategory: MessageCategory,
    categoryCounts: Map<MessageCategory, Int>,
    onSelectCategory: (MessageCategory) -> Unit,
    visibleCategories: List<MessageCategory> = MessageCategory.values().toList(),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visibleCategories.forEach { category ->
            val isSelected = (selectedCategory == category)
            val count = categoryCounts[category] ?: 0

            val icon = when (category) {
                MessageCategory.ALL -> Icons.Default.Chat
                MessageCategory.PERSONAL -> Icons.Default.Person
                MessageCategory.OTP -> Icons.Default.Key
                MessageCategory.TRANSACTIONS -> Icons.Default.AccountBalance
                MessageCategory.SPAM -> Icons.Default.Block
            }

            val iconColor = if (isSelected) {
                Color.White
            } else {
                when (category) {
                    MessageCategory.OTP -> OtpAmber
                    MessageCategory.TRANSACTIONS -> TransactionGreen
                    MessageCategory.SPAM -> SpamRed
                    else -> PurplePrimary
                }
            }

            val chipBg = if (isSelected) PurplePrimary else Color.White
            val textColor = if (isSelected) Color.White else TextDark
            val shape = RoundedCornerShape(20.dp)

            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(chipBg)
                    .then(
                        if (!isSelected) Modifier.border(1.dp, DividerColor, shape) else Modifier
                    )
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.displayName,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = category.displayName,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )

                    if (count > 0 && category != MessageCategory.ALL) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
