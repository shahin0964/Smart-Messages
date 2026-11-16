package com.smartmessages.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartmessages.app.data.model.MessageCategory
import com.smartmessages.app.ui.theme.OtpAmber
import com.smartmessages.app.ui.theme.SpamRed
import com.smartmessages.app.ui.theme.TransactionGreen

@Composable
fun SmartCategoryChips(
    selectedCategory: MessageCategory,
    categoryCounts: Map<MessageCategory, Int>,
    onSelectCategory: (MessageCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MessageCategory.values().forEach { category ->
            val isSelected = (selectedCategory == category)
            val count = categoryCounts[category] ?: 0

            val icon = when (category) {
                MessageCategory.ALL -> Icons.Default.Chat
                MessageCategory.PERSONAL -> Icons.Default.Person
                MessageCategory.OTP -> Icons.Default.Key
                MessageCategory.TRANSACTIONS -> Icons.Default.AccountBalance
                MessageCategory.SPAM -> Icons.Default.Block
            }

            val iconTint = when (category) {
                MessageCategory.OTP -> OtpAmber
                MessageCategory.TRANSACTIONS -> TransactionGreen
                MessageCategory.SPAM -> SpamRed
                else -> MaterialTheme.colorScheme.primary
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    val labelText = if (count > 0 && category != MessageCategory.ALL) {
                        "${category.displayName} ($count)"
                    } else {
                        category.displayName
                    }
                    Text(text = labelText)
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.displayName,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
