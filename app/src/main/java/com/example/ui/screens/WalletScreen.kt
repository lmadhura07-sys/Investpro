package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppStrings
import com.example.data.model.ActiveInvestmentPlan
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.components.ActivityRow
import com.example.ui.components.TransactionDetailsDialog
import com.example.ui.theme.BonusGold
import com.example.ui.theme.BonusGoldLight
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun WalletScreen(
    availableCash: Double,
    activePlans: List<ActiveInvestmentPlan>,
    transactions: List<TransactionRecord>,
    strings: AppStrings,
    onOpenDeposit: () -> Unit,
    onOpenWithdrawCash: () -> Unit,
    onEarlyWithdrawClick: (ActiveInvestmentPlan) -> Unit,
    onMaturityWithdrawClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionRecord?>(null) }

    val totalInvestedCapital = activePlans.sumOf { it.depositAmount }
    val totalGrowingValue = activePlans.sumOf { it.currentGrowingValue }

    // Aggregate statistics across transaction types
    val totalDeposits = remember(transactions) {
        transactions.filter { it.type == TransactionType.DEPOSIT }.sumOf { it.amountLkr }
    }
    val totalWithdrawals = remember(transactions) {
        transactions.filter {
            it.type == TransactionType.EARLY_WITHDRAWAL || it.type == TransactionType.GAME_WALLET_WITHDRAWAL
        }.sumOf { abs(it.amountLkr) }
    }
    val totalGameEarnings = remember(transactions) {
        transactions.filter {
            it.type == TransactionType.GAME_WINNING || it.type == TransactionType.GAME_TO_MAIN_TRANSFER
        }.sumOf { it.amountLkr }
    }

    // Comprehensive transaction filtering: Type, Status, and Search Query
    val filteredList = remember(transactions, selectedFilter, selectedStatusFilter, searchQuery) {
        transactions.filter { tx ->
            // 1. Category / Type Filter
            val matchesType = when (selectedFilter) {
                "DEPOSITS" -> tx.type == TransactionType.DEPOSIT
                "WITHDRAWALS" -> tx.type == TransactionType.EARLY_WITHDRAWAL || tx.type == TransactionType.GAME_WALLET_WITHDRAWAL
                "GAME EARNINGS" -> tx.type == TransactionType.GAME_WINNING || tx.type == TransactionType.GAME_TO_MAIN_TRANSFER || tx.type == TransactionType.GAME_FEE
                "INVESTMENTS" -> tx.type == TransactionType.PLAN_INVESTMENT || tx.type == TransactionType.MATURITY_PAYOUT || tx.type == TransactionType.VIP_BONUS
                else -> true
            }

            // 2. Status Filter
            val matchesStatus = when (selectedStatusFilter) {
                "COMPLETED" -> tx.status == TransactionStatus.COMPLETED
                "PENDING" -> tx.status == TransactionStatus.PENDING
                "APPROVED" -> tx.status == TransactionStatus.APPROVED
                else -> true
            }

            // 3. Search Query Filter (Title, Reference, Destination, Payment Method)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                tx.title.lowercase().contains(q) ||
                tx.reference.lowercase().contains(q) ||
                tx.destination.lowercase().contains(q) ||
                tx.method.displayName.lowercase().contains(q)
            }

            matchesType && matchesStatus && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.walletTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Deposits, Withdrawals & Ledger",
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = strings.minWithdrawalInfo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }
            }
        }

        // Available Balance & Asset Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "AVAILABLE CASH IN WALLET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${"%,.2f".format(availableCash)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Growing Plans Value", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Rs. ${"%,.0f".format(totalGrowingValue)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryBlue
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Locked Capital", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Rs. ${"%,.0f".format(totalInvestedCapital)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenDeposit,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("wallet_deposit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.depositFunds, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = onOpenWithdrawCash,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("wallet_withdraw_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CallMade, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.withdraw, fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Active Plans Section with Early/Maturity Withdrawal options
        if (activePlans.isNotEmpty()) {
            item {
                Text(
                    text = "Active Investment Capital (${activePlans.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(activePlans, key = { it.id }) { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.planDuration.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Deposit: Rs. ${"%,.0f".format(plan.depositAmount)}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Current: Rs. ${"%,.0f".format(plan.currentGrowingValue)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                Text(
                                    if (plan.isMatured) "Ready for Full Payout" else "Month ${plan.currentMonth} of ${plan.planDuration.months}",
                                    fontSize = 10.sp,
                                    color = if (plan.isMatured) SuccessGreen else Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (plan.isMatured) {
                            Button(
                                onClick = { onMaturityWithdrawClick(plan.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(38.dp)
                            ) {
                                Text("Claim Payout: Rs. ${"%,.0f".format(plan.finalMaturityAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { onEarlyWithdrawClick(plan) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(38.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Early Withdrawal (Principal Only: Rs. ${"%,.0f".format(plan.depositAmount)})", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Channels Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PayoutChannelBadge("Commercial / BOC", Icons.Default.AccountBalance)
                    Text("•", color = Color(0xFFCFD8DC))
                    PayoutChannelBadge("Dialog eZ Cash", Icons.Default.PhoneAndroid)
                    Text("•", color = Color(0xFFCFD8DC))
                    PayoutChannelBadge("USDT (TRC-20)", Icons.Default.Payment)
                }
            }
        }

        // Recent Transactions Section Header & Controls
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_transactions_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = ButtonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Transaction List",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("recent_transactions_header")
                            )
                            Text(
                                text = "Tap any entry to view full details",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${filteredList.size} Records",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary Metric Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionMetricChip(
                        title = "Deposits",
                        amount = totalDeposits,
                        color = SuccessGreen,
                        icon = Icons.Default.CallReceived,
                        modifier = Modifier.weight(1f)
                    )
                    TransactionMetricChip(
                        title = "Withdrawals",
                        amount = totalWithdrawals,
                        color = Color(0xFFD32F2F),
                        icon = Icons.Default.CallMade,
                        modifier = Modifier.weight(1f)
                    )
                    TransactionMetricChip(
                        title = "Game Wins",
                        amount = totalGameEarnings,
                        color = BonusGold,
                        icon = Icons.Default.SportsEsports,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Transaction Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_search_input"),
                    placeholder = {
                        Text(
                            "Search by reference, channel, or title...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = ButtonGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 1. Category Filter Chips (Horizontal Scroll)
                val categoryScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(categoryScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "DEPOSITS", "WITHDRAWALS", "GAME EARNINGS", "INVESTMENTS").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("filter_chip_${filter.lowercase().replace(" ", "_")}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F8EE),
                                selectedLabelColor = ButtonGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Status Filter Chips (ALL, COMPLETED, PENDING, APPROVED)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "COMPLETED", "PENDING", "APPROVED").forEach { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                            label = { Text(status, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("status_filter_${status.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE3F2FD),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            }
        }

        // Transactions List or Empty State
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("empty_transactions_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No matches for \"$searchQuery\""
                            } else when (selectedFilter) {
                                "DEPOSITS" -> "No deposits recorded yet"
                                "WITHDRAWALS" -> "No withdrawals recorded yet"
                                "GAME EARNINGS" -> "No game earnings recorded yet"
                                "INVESTMENTS" -> "No investments recorded yet"
                                else -> "No transactions found"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Try searching by another reference number or keyword"
                            } else when (selectedFilter) {
                                "DEPOSITS" -> "Deposit funds to begin investing in high profit plans"
                                "WITHDRAWALS" -> "Payout requests and withdrawals will appear here"
                                "GAME EARNINGS" -> "Play Spin & Win, Tap Tap Coin, or Number Guess to earn"
                                "INVESTMENTS" -> "Start a 3, 6, or 12 months plan to see growing capital"
                                else -> "Your past transactions with live status icons will be listed here"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { tx ->
                ActivityRow(
                    tx = tx,
                    onClick = { selectedTransactionForDetails = tx }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Transaction Details Modal
    selectedTransactionForDetails?.let { tx ->
        TransactionDetailsDialog(
            transaction = tx,
            onDismiss = { selectedTransactionForDetails = null }
        )
    }
}

@Composable
private fun TransactionMetricChip(
    title: String,
    amount: Double,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rs. ${"%,.0f".format(amount)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
fun PayoutChannelBadge(name: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkBlue)
    }
}
