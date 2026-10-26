package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.ActivityRow
import com.example.ui.components.SavingsTargetCard
import com.example.ui.components.TransactionDetailsDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.localization.AppStrings
import com.example.data.model.ActiveInvestmentPlan
import com.example.data.model.PlanDuration
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.ui.theme.BonusGold
import com.example.ui.theme.BonusGoldLight
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.LightBlueBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    profile: UserProfile,
    availableCash: Double,
    activePlans: List<ActiveInvestmentPlan>,
    recentTransactions: List<TransactionRecord>,
    strings: AppStrings,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onDepositClick: () -> Unit,
    onInvestClick: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onAdvanceMonth: (String) -> Unit,
    onEarlyWithdrawClick: (ActiveInvestmentPlan) -> Unit,
    onMaturityWithdrawClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var selectedTxForDetails by remember { mutableStateOf<TransactionRecord?>(null) }

    val totalInvestedCapital = activePlans.sumOf { it.depositAmount }
    val totalGrowingValue = activePlans.sumOf { it.currentGrowingValue }
    val totalAccruedProfit = totalGrowingValue - totalInvestedCapital
    val totalNetWorth = availableCash + totalGrowingValue

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBBDEFB)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.profilePhotoUri != null) {
                            AsyncImage(
                                model = profile.profilePhotoUri,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = profile.avatarLetter,
                                color = DarkBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val displayName = if (profile.fullName.isNotBlank()) profile.fullName else "Investor"
                        Text(
                            text = "${strings.greeting}, $displayName 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = profile.maskedPhoneNumber,
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Top Right Action Buttons: Theme Switch (Sun/Moon) & Notifications
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        if (isDarkMode) {
                            Icon(
                                Icons.Default.LightMode,
                                contentDescription = "Switch to Light Mode",
                                tint = BonusGold,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.DarkMode,
                                contentDescription = "Switch to Dark Mode",
                                tint = DarkBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { /* Notifications */ },
                        modifier = Modifier.testTag("notification_bell")
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (isDarkMode) Color.White else DarkBlue
                        )
                    }
                }
            }
        }

        // Center Blue Gradient Card: Portfolio Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_center_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1976D2),
                                    Color(0xFF1565C0),
                                    Color(0xFF0D47A1)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.currentGrowingValue,
                                color = Color(0xFFBBDEFB),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${activePlans.size} Active Plans",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Rs. ${"%,.2f".format(totalNetWorth)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Cash: Rs. ${"%,.0f".format(availableCash)}",
                                fontSize = 12.sp,
                                color = Color(0xFFE3F2FD)
                            )
                            Text(
                                text = "•",
                                color = Color(0xFFBBDEFB)
                            )
                            Text(
                                text = "Growing Capital: Rs. ${"%,.0f".format(totalInvestedCapital)}",
                                fontSize = 12.sp,
                                color = Color(0xFFE3F2FD)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Accrued Profit Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.totalProfitAccrued,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "+Rs. ${"%,.2f".format(totalAccruedProfit)}",
                                color = SuccessGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Two Primary Action Buttons: [ Deposit Funds ] [ Invest in Plan ]
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("dashboard_deposit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.depositFunds,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onInvestClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("dashboard_invest_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = DarkBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.investNow,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Savings Target Progress Ring / Bar Chart (Rs. 5,000 target)
        item {
            SavingsTargetCard(
                currentSavings = totalNetWorth,
                initialTarget = 5000.0,
                onDepositClick = onDepositClick
            )
        }

        // Active Investment Plan Showcase
        item {
            Text(
                text = strings.activePlan,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        }

        if (activePlans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPlans() }
                        .testTag("no_plans_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Your High Profit Plan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkBlue
                            )
                            Text(
                                text = "Earn 50%, 120% or 300% profit (Min. Rs. 100 deposit)",
                                fontSize = 11.sp,
                                color = Color(0xFF546E7A)
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = PrimaryBlue)
                    }
                }
            }
        } else {
            items(activePlans, key = { it.id }) { plan ->
                ActivePlanCard(
                    plan = plan,
                    strings = strings,
                    dateFormatter = dateFormatter,
                    onAdvanceMonth = { onAdvanceMonth(plan.id) },
                    onEarlyWithdraw = { onEarlyWithdrawClick(plan) },
                    onMaturityWithdraw = { onMaturityWithdrawClick(plan.id) }
                )
            }
        }

        // 3 High Profit Plans Quick Showcase
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.plansTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onNavigateToPlans() }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlanBadgeCard(
                    title = "3 Months",
                    profit = "50%",
                    multiplier = "1.5X",
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
                PlanBadgeCard(
                    title = "6 Months",
                    profit = "120%",
                    multiplier = "2.2X",
                    color = Color(0xFF0D47A1),
                    modifier = Modifier.weight(1f)
                )
                PlanBadgeCard(
                    title = "12 Months",
                    profit = "300%",
                    multiplier = "4.0X",
                    color = Color(0xFF00796B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Activity Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.recentActivity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Text(
                    text = "${recentTransactions.size} Records",
                    fontSize = 12.sp,
                    color = Color(0xFF546E7A)
                )
            }
        }

        items(recentTransactions.take(5), key = { it.id }) { tx ->
            ActivityRow(
                tx = tx,
                onClick = { selectedTxForDetails = tx }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    selectedTxForDetails?.let { tx ->
        TransactionDetailsDialog(
            transaction = tx,
            onDismiss = { selectedTxForDetails = null }
        )
    }
}

@Composable
fun ActivePlanCard(
    plan: ActiveInvestmentPlan,
    strings: AppStrings,
    dateFormatter: SimpleDateFormat,
    onAdvanceMonth: () -> Unit,
    onEarlyWithdraw: () -> Unit,
    onMaturityWithdraw: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_plan_${plan.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Plan Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.planDuration.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DarkBlue
                    )
                    if (plan.isVip) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BonusGoldLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("VIP +10%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7E3B00))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (plan.isMatured) SuccessGreenLight else Color(0xFFE3F2FD))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (plan.isMatured) "MATURED 🏆" else "Month ${plan.currentMonth} of ${plan.planDuration.months}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (plan.isMatured) SuccessGreen else PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Initial Deposit", fontSize = 11.sp, color = Color.Gray)
                    Text("Rs. ${"%,.0f".format(plan.depositAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkBlue)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Profit Multiplier", fontSize = 11.sp, color = Color.Gray)
                    Text("${plan.totalProfitPercent.toInt()}% (${plan.planDuration.multiplierText})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Current Value", fontSize = 11.sp, color = Color.Gray)
                    Text("Rs. ${"%,.0f".format(plan.currentGrowingValue)}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Bar
            val progressFraction = (plan.currentMonth.toFloat() / plan.planDuration.months.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (plan.isMatured) SuccessGreen else PrimaryBlue,
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (plan.isMatured) "Ready for payout!" else strings.growingStatus,
                    fontSize = 11.sp,
                    color = if (plan.isMatured) SuccessGreen else Color(0xFF546E7A),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Matures: ${dateFormatter.format(Date(plan.maturityDate))}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Demo advance + Early withdrawal / Maturity claim
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!plan.isMatured) {
                    // Demo simulation advance
                    OutlinedButton(
                        onClick = onAdvanceMonth,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate +1 Mo", fontSize = 11.sp)
                    }

                    // Early withdrawal button with warning
                    Button(
                        onClick = onEarlyWithdraw,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.withdraw, color = Color(0xFFD32F2F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Matured: Full payout button
                    Button(
                        onClick = onMaturityWithdraw,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Claim Payout: Rs. ${"%,.0f".format(plan.finalMaturityAmount)}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlanBadgeCard(
    title: String,
    profit: String,
    multiplier: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(profit, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Text(multiplier, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        }
    }
}
