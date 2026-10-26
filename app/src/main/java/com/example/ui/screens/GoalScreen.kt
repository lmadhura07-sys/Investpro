package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppStrings
import com.example.data.model.ActiveInvestmentPlan
import com.example.data.model.PlanDuration
import com.example.ui.theme.BonusGold
import com.example.ui.theme.BonusGoldLight
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.LightBlueBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight

@Composable
fun GoalScreen(
    availableCash: Double,
    activePlans: List<ActiveInvestmentPlan>,
    strings: AppStrings,
    onOpenInvestModal: (PlanDuration) -> Unit,
    onInvestDirectly: (Double, PlanDuration) -> Unit,
    onDepositClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var calcAmountText by remember { mutableStateOf("10000") }
    var calcDuration by remember { mutableStateOf(PlanDuration.MONTHS_6) }

    val calcAmount = calcAmountText.toDoubleOrNull() ?: 0.0
    val isAmountValid = calcAmount >= 100.0
    val isVip = calcAmount >= 100000.0
    val effectiveProfitPercent = calcDuration.profitPercent + if (isVip) 10.0 else 0.0
    val profitAmount = (calcAmount * effectiveProfitPercent) / 100.0
    val maturityTotal = calcAmount + profitAmount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.plansTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Text(
                        text = strings.plansSubtitle,
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Cash: Rs. ${"%,.0f".format(availableCash)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }
            }
        }

        // VIP Bonus Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BonusGoldLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BonusGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "VIP Extra Profit Multiplier",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF7E3B00)
                        )
                        Text(
                            text = strings.vipBonusNotice,
                            fontSize = 11.sp,
                            color = Color(0xFF5D2E00),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3 High Profit Plan Options
        item {
            Text(
                text = "Choose Your Growth Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        }

        // Plan 1: 3 Months (50% Profit)
        item {
            PlanCard(
                duration = PlanDuration.MONTHS_3,
                title = strings.plan3MoTitle,
                profitRateText = strings.profitRate50,
                multiplierText = "1.5X Return",
                description = "Rapid short-term growth plan. Earn 50% profit in 3 months.",
                badgeColor = Color(0xFF1976D2),
                onSelect = { onOpenInvestModal(PlanDuration.MONTHS_3) }
            )
        }

        // Plan 2: 6 Months (120% Profit)
        item {
            PlanCard(
                duration = PlanDuration.MONTHS_6,
                title = strings.plan6MoTitle,
                profitRateText = strings.profitRate120,
                multiplierText = "2.2X Return",
                description = "More than double your capital with 120% profit return.",
                badgeColor = Color(0xFF0D47A1),
                isPopular = true,
                onSelect = { onOpenInvestModal(PlanDuration.MONTHS_6) }
            )
        }

        // Plan 3: 12 Months (300% Profit)
        item {
            PlanCard(
                duration = PlanDuration.MONTHS_12,
                title = strings.plan12MoTitle,
                profitRateText = strings.profitRate300,
                multiplierText = "4.0X Return",
                description = "Maximum wealth compounding. 300% guaranteed profit at maturity.",
                badgeColor = Color(0xFF00796B),
                onSelect = { onOpenInvestModal(PlanDuration.MONTHS_12) }
            )
        }

        // Live Profit Return Calculator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profit_calculator_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.calculateReturns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Plan selection chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlanDuration.values().forEach { duration ->
                            FilterChip(
                                selected = calcDuration == duration,
                                onClick = { calcDuration = duration },
                                label = { Text("${duration.months} Mo (${duration.profitPercent.toInt()}%)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deposit amount input
                    OutlinedTextField(
                        value = calcAmountText,
                        onValueChange = { calcAmountText = it },
                        label = { Text("Simulated Deposit (LKR)") },
                        prefix = { Text("Rs. ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_amount_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculation Table
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Deposit Capital:", fontSize = 12.sp, color = Color(0xFF546E7A))
                                Text("Rs. ${"%,.0f".format(calcAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkBlue)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Guaranteed Profit (${effectiveProfitPercent.toInt()}%):",
                                    fontSize = 12.sp,
                                    color = SuccessGreen
                                )
                                Text("+Rs. ${"%,.0f".format(profitAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                            }
                            if (isVip) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("VIP Bonus Included:", fontSize = 11.sp, color = Color(0xFF7E3B00))
                                    Text("+10% Extra", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF7E3B00))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Payout at Maturity:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                                Text("Rs. ${"%,.0f".format(maturityTotal)}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = PrimaryBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (availableCash >= calcAmount && isAmountValid) {
                        Button(
                            onClick = { onInvestDirectly(calcAmount, calcDuration) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("calc_invest_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Invest Rs. ${"%,.0f".format(calcAmount)} Now", fontWeight = FontWeight.Bold)
                        }
                    } else if (!isAmountValid) {
                        Text(
                            text = strings.minDepositError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = onDepositClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("calc_deposit_funds_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Deposit Funds (Min Rs. 100)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PlanCard(
    duration: PlanDuration,
    title: String,
    profitRateText: String,
    multiplierText: String,
    description: String,
    badgeColor: Color,
    isPopular: Boolean = false,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("plan_card_${duration.name}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkBlue
                    )
                    if (isPopular) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreenLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("POPULAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = multiplierText,
                        color = badgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = profitRateText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = SuccessGreen
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF546E7A),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Invest in ${duration.months}M Plan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
