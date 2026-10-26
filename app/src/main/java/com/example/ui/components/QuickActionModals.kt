package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.localization.AppStrings
import com.example.data.model.ActiveInvestmentPlan
import com.example.data.model.PaymentMethod
import com.example.data.model.PlanDuration
import com.example.ui.theme.BonusGold
import com.example.ui.theme.BonusGoldLight
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight

@Composable
fun DepositDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod) -> Unit
) {
    var amountText by remember { mutableStateOf("1000") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.LANKA_QR) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isDepositValid = amount >= 100.0 && amount <= 500000.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.depositFunds,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Text(
                            text = "Instant credit to your private +94 wallet",
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_deposit_modal")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Deposit Amount Input with Strict Rs 100 Minimum
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Deposit Amount (Min. Rs. 100)") },
                    prefix = { Text("Rs. ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input"),
                    singleLine = true,
                    isError = amountText.isNotEmpty() && !isDepositValid,
                    supportingText = {
                        if (amountText.isNotEmpty() && amount < 100.0) {
                            Text(strings.minDepositError, color = MaterialTheme.colorScheme.error)
                        } else if (amount >= 100000.0) {
                            Text("⭐ VIP Bonus Qualified (+10% Extra Profit on plans!)", color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(500.0, 1000.0, 5000.0, 100000.0).forEach { preset ->
                        FilterChip(
                            selected = amount == preset,
                            onClick = { amountText = preset.toInt().toString() },
                            label = {
                                Text(
                                    if (preset >= 100000.0) "Rs. 100k (VIP)" else "Rs. ${preset.toInt()}",
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Payment Method:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                PaymentMethod.values().forEach { method ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedMethod = method },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMethod == method) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (method) {
                                PaymentMethod.LANKA_QR -> Icons.Default.QrCode2
                                PaymentMethod.EZ_CASH -> Icons.Default.PhoneAndroid
                                PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                                PaymentMethod.USDT -> Icons.Default.Payment
                            }
                            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(method.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            if (selectedMethod == method) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (isDepositValid) onConfirm(amount, selectedMethod)
                    },
                    enabled = isDepositValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_deposit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Deposit Rs. ${"%,.0f".format(amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InvestInPlanDialog(
    strings: AppStrings,
    availableCash: Double,
    initialDuration: PlanDuration = PlanDuration.MONTHS_6,
    onDismiss: () -> Unit,
    onConfirmInvest: (amount: Double, duration: PlanDuration) -> Unit,
    onNeedsDeposit: () -> Unit
) {
    var amountText by remember { mutableStateOf("1000") }
    var selectedDuration by remember { mutableStateOf(initialDuration) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isAmountValid = amount >= 100.0
    val hasEnoughCash = availableCash >= amount

    val isVip = amount >= 100000.0
    val effectiveProfitPercent = selectedDuration.profitPercent + if (isVip) 10.0 else 0.0
    val totalProfit = (amount * effectiveProfitPercent) / 100.0
    val totalMaturityPayout = amount + totalProfit

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.investNow,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Text(
                            text = "Choose high profit plan duration",
                            fontSize = 12.sp,
                            color = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_invest_modal")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Plan
                PlanDuration.values().forEach { dur ->
                    val isSelected = selectedDuration == dur
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedDuration = dur },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(dur.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkBlue)
                                Text(
                                    text = "${dur.profitPercent.toInt()}% Guaranteed Profit (${dur.multiplierText})",
                                    fontSize = 12.sp,
                                    color = if (isSelected) PrimaryBlue else Color(0xFF546E7A)
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Investment Amount (Min. Rs. 100)") },
                    prefix = { Text("Rs. ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invest_amount_input"),
                    singleLine = true,
                    isError = amountText.isNotEmpty() && !isAmountValid
                )

                Spacer(modifier = Modifier.height(10.dp))

                // VIP Notice if >= 100,000
                if (isVip) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BonusGoldLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = BonusGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VIP Tier: Extra +10% Profit Applied! (${effectiveProfitPercent.toInt()}% Total)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7E3B00)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Profit Calculation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Capital:", fontSize = 12.sp, color = Color(0xFF546E7A))
                            Text("Rs. ${"%,.0f".format(amount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Expected Profit (${effectiveProfitPercent.toInt()}%):", fontSize = 12.sp, color = SuccessGreen)
                            Text("+Rs. ${"%,.0f".format(totalProfit)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total at Maturity:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                            Text("Rs. ${"%,.0f".format(totalMaturityPayout)}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = DarkBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cash in Wallet:", fontSize = 12.sp, color = Color.Gray)
                    Text("Rs. ${"%,.2f".format(availableCash)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkBlue)
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (hasEnoughCash) {
                    Button(
                        onClick = {
                            if (isAmountValid) onConfirmInvest(amount, selectedDuration)
                        },
                        enabled = isAmountValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_invest_plan_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Lock In & Start Growing (Rs. ${"%,.0f".format(amount)})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onDismiss()
                            onNeedsDeposit()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("deposit_needed_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Deposit Funds First (Need Rs. ${"%,.0f".format(amount - availableCash)})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Early Withdrawal Warning System (Spec Requirement):
 * "Your plan is still growing. If you withdraw now, you will get only your main deposit, no profit. Do you want to continue?"
 */
@Composable
fun EarlyWithdrawalWarningDialog(
    plan: ActiveInvestmentPlan,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirmEarlyWithdrawal: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = strings.earlyWithdrawalWarningTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DarkBlue,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = strings.earlyWithdrawalWarningBody,
                    fontSize = 14.sp,
                    color = Color(0xFF37474F),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Main Deposit:", fontSize = 12.sp, color = Color(0xFFB71C1C))
                            Text("Rs. ${"%,.0f".format(plan.depositAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Forfeited Profit:", fontSize = 12.sp, color = Color(0xFFB71C1C))
                            Text("-Rs. ${"%,.0f".format(plan.totalProfitAmount)} (Lost)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmEarlyWithdrawal,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_early_withdraw_yes")
            ) {
                Text(strings.withdrawPrincipalOnly, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("keep_growing_no")
            ) {
                Text(strings.keepGrowing, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkBlue)
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White
    )
}

@Composable
fun WithdrawCashDialog(
    availableBalance: Double,
    savedAccount: String,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirmWithdrawal: (amount: Double, method: PaymentMethod, destination: String) -> Unit
) {
    var amountText by remember { mutableStateOf("500") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
    var destinationInput by remember { mutableStateOf(savedAccount) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValidAmount = amount >= 200.0 && amount <= availableBalance

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.withdrawFunds,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_withdraw_modal")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.minWithdrawalInfo,
                    fontSize = 12.sp,
                    color = Color(0xFF546E7A)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available Cash:", fontSize = 12.sp, color = DarkBlue)
                        Text(
                            "Rs. ${"%,.2f".format(availableBalance)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = DarkBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (Min. Rs. 200)") },
                    prefix = { Text("Rs. ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input"),
                    singleLine = true,
                    isError = amount > 0 && !isValidAmount,
                    supportingText = {
                        if (amount > 0 && amount < 200.0) {
                            Text("Minimum withdrawal is Rs. 200", color = MaterialTheme.colorScheme.error)
                        } else if (amount > availableBalance) {
                            Text("Amount exceeds available balance", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Payout Method:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkBlue)
                Spacer(modifier = Modifier.height(6.dp))

                listOf(PaymentMethod.BANK_TRANSFER, PaymentMethod.EZ_CASH, PaymentMethod.USDT).forEach { method ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { selectedMethod = method },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMethod == method) Color(0xFFD1E4FA) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(method.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (selectedMethod == method) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = destinationInput,
                    onValueChange = { destinationInput = it },
                    label = {
                        Text(
                            when (selectedMethod) {
                                PaymentMethod.BANK_TRANSFER -> "Bank Account & Branch"
                                PaymentMethod.EZ_CASH -> "eZ Cash Mobile Number"
                                PaymentMethod.USDT -> "USDT (TRC-20) Address"
                                else -> "Destination Account"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_destination_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (isValidAmount) onConfirmWithdrawal(amount, selectedMethod, destinationInput)
                    },
                    enabled = isValidAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_withdraw_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Submit Payout (Rs. ${"%,.0f".format(amount)})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
