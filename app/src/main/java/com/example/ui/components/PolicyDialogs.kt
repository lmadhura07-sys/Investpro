package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = ButtonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Privacy Policy",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your Data & Account Security",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Strict Security Rule Highlight Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth().testTag("privacy_security_rule_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = ButtonGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "STRICT DATA ISOLATION RULE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ButtonGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Every user can ONLY access their own +94 private folder. No user or third party can view, edit, or leak your balances, profile details, or financial ledger.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B5E20),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                PolicySection(
                    title = "1. Phone Number & Identity (+94)",
                    body = "We use your unique Sri Lankan phone number (+94) as the sole identifier for your vault. Your number is never sold or shared with advertisers."
                )

                PolicySection(
                    title = "2. One Phone = One Account",
                    body = "To prevent duplicate accounts and fraud, each +94 phone number is mapped to exactly one account. Device credentials securely prevent multi-account tampering."
                )

                PolicySection(
                    title = "3. Profile Photo & Storage",
                    body = "Photos captured or selected from your gallery are kept encrypted within your designated +94 folder. You retain complete freedom to replace or remove your photo at any time."
                )

                PolicySection(
                    title = "4. Bank & Payment Information",
                    body = "Bank account numbers, Dialog eZ Cash details, and crypto wallet addresses provided for deposits and payouts are strictly encrypted and used solely to process requested withdrawals."
                )

                PolicySection(
                    title = "5. Financial Privacy & Encryption",
                    body = "All transactions and investment records are encrypted at rest and in transit in full compliance with modern financial cybersecurity standards."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("privacy_policy_close_btn")
            ) {
                Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun TermsConditionsDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Terms & Conditions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "InvestPro High Profit Guidelines",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PolicySection(
                    title = "1. Minimum Deposit",
                    body = "The strict minimum deposit is Rs. 100. Users can deposit amounts up to Rs. 500,000 per transaction using Bank Transfer, Dialog eZ Cash, or LankaQR."
                )

                PolicySection(
                    title = "2. High Profit Investment Plans",
                    body = "InvestPro offers three guaranteed growth plans:\n" +
                            "• 3 Months Plan: 50% Total Profit (1.5x)\n" +
                            "• 6 Months Plan: 120% Total Profit (2.2x)\n" +
                            "• 12 Months Plan: 300% Total Profit (4.0x)\n" +
                            "Deposits of Rs. 100,000 or more automatically qualify for an extra +10% VIP Bonus on profit yield."
                )

                PolicySection(
                    title = "3. Maturity & Early Withdrawal Rule",
                    body = "Full maturity payouts (Capital + Accrued Profit) become claimable once the full plan duration finishes. If a user chooses early withdrawal before plan maturity, ONLY the original principal deposit is refunded — all pending profits are forfeited."
                )

                PolicySection(
                    title = "4. Two-Wallet Separation & Game Rules",
                    body = "• Main Wallet: Holds investment capital, deposits, and maturity returns.\n" +
                            "• Game Wallet: Holds earnings from Spin & Win, Tap Tap Coin, and Number Guess.\n" +
                            "• Game Wallet Minimum Withdrawal: Rs. 200.\n" +
                            "• Daily Earnings Cap: Rs. 3,000 per calendar day.\n" +
                            "• Transfers from Game Wallet to Main Wallet receive a 5% bonus if balance exceeds Rs. 1,000."
                )

                PolicySection(
                    title = "5. Account Security Rule",
                    body = "One +94 phone number is permitted per individual. Cross-account usage, fraudulent bots, or tampering with phone verifications will result in account restriction."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("terms_conditions_accept_btn")
            ) {
                Text("I Understand & Accept", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun PolicySection(
    title: String,
    body: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 16.sp
        )
    }
}
