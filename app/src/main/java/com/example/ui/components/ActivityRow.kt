package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.theme.BonusGold
import com.example.ui.theme.BonusGoldLight
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

import androidx.compose.foundation.clickable

@Composable
fun ActivityRow(
    tx: TransactionRecord,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isPositive = tx.amountLkr > 0
    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_row_${tx.id}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category / Type Icon Box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when (tx.type) {
                            TransactionType.DEPOSIT -> Color(0xFFE3F2FD)
                            TransactionType.PLAN_INVESTMENT -> Color(0xFFEDE7F6)
                            TransactionType.MATURITY_PAYOUT -> SuccessGreenLight
                            TransactionType.EARLY_WITHDRAWAL -> Color(0xFFFFEBEE)
                            TransactionType.VIP_BONUS -> BonusGoldLight
                            TransactionType.GAME_WINNING -> Color(0xFFE8F5E9)
                            TransactionType.GAME_FEE -> Color(0xFFFFF3E0)
                            TransactionType.GAME_WALLET_WITHDRAWAL -> Color(0xFFFFEBEE)
                            TransactionType.GAME_TO_MAIN_TRANSFER -> Color(0xFFE1F5FE)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (tx.type) {
                        TransactionType.DEPOSIT -> Icons.Default.CallReceived
                        TransactionType.PLAN_INVESTMENT -> Icons.Default.Savings
                        TransactionType.MATURITY_PAYOUT -> Icons.Default.CheckCircle
                        TransactionType.EARLY_WITHDRAWAL -> Icons.Default.CallMade
                        TransactionType.VIP_BONUS -> Icons.Default.Star
                        TransactionType.GAME_WINNING -> Icons.Default.SportsEsports
                        TransactionType.GAME_FEE -> Icons.Default.SportsEsports
                        TransactionType.GAME_WALLET_WITHDRAWAL -> Icons.Default.CallMade
                        TransactionType.GAME_TO_MAIN_TRANSFER -> Icons.Default.CallReceived
                    },
                    contentDescription = tx.type.name,
                    tint = when (tx.type) {
                        TransactionType.DEPOSIT -> PrimaryBlue
                        TransactionType.PLAN_INVESTMENT -> Color(0xFF5E35B1)
                        TransactionType.MATURITY_PAYOUT -> SuccessGreen
                        TransactionType.EARLY_WITHDRAWAL -> Color(0xFFD32F2F)
                        TransactionType.VIP_BONUS -> BonusGold
                        TransactionType.GAME_WINNING -> SuccessGreen
                        TransactionType.GAME_FEE -> Color(0xFFE65100)
                        TransactionType.GAME_WALLET_WITHDRAWAL -> Color(0xFFD32F2F)
                        TransactionType.GAME_TO_MAIN_TRANSFER -> PrimaryBlue
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${tx.destination} • ${tx.method.displayName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = dateFormatter.format(Date(tx.timestamp)),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            // Amount & Status Badge with Status Icon
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isPositive) "+Rs. " else "-Rs. ") + "%,.2f".format(abs(tx.amountLkr)),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = if (isPositive) SuccessGreen else Color(0xFFD32F2F)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status Badge with icon
                val statusBg = when (tx.status) {
                    TransactionStatus.COMPLETED -> Color(0xFFE8F5E9)
                    TransactionStatus.APPROVED -> Color(0xFFE3F2FD)
                    TransactionStatus.PENDING -> Color(0xFFFFF3E0)
                }
                val statusColor = when (tx.status) {
                    TransactionStatus.COMPLETED -> SuccessGreen
                    TransactionStatus.APPROVED -> PrimaryBlue
                    TransactionStatus.PENDING -> Color(0xFFE65100)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("tx_status_badge_${tx.id}")
                ) {
                    Icon(
                        imageVector = when (tx.status) {
                            TransactionStatus.COMPLETED -> Icons.Default.CheckCircle
                            TransactionStatus.APPROVED -> Icons.Default.CheckCircle
                            TransactionStatus.PENDING -> Icons.Default.Schedule
                        },
                        contentDescription = tx.status.name,
                        tint = statusColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = when (tx.status) {
                            TransactionStatus.COMPLETED -> "Completed"
                            TransactionStatus.APPROVED -> "Approved"
                            TransactionStatus.PENDING -> "Pending"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}
