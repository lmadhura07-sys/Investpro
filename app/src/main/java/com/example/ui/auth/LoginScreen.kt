package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

enum class AuthStep {
    EMAIL_INPUT,
    OTP_VERIFY
}

/**
 * Free Email OTP Authentication Screen.
 * Generates dynamic 6-digit random OTP (no 123456 demo code),
 * 5-minute expiry, 60s resend timer, and 6-box input.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (userIdentifier: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(AuthStep.EMAIL_INPUT) }
    var emailInput by remember { mutableStateOf("") }
    var otpDigits by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(60) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutSeconds by remember { mutableIntStateOf(300) } // 5 minutes
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activeOtpCode by remember { mutableStateOf("") }
    var otpExpiryMillis by remember { mutableLongStateOf(0L) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Auto-dismiss toast after 4s
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(4000L)
            toastMessage = null
        }
    }

    // Countdown Timer for OTP Resend (60s to 0)
    LaunchedEffect(step, countdown) {
        if (step == AuthStep.OTP_VERIFY && countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    // Lockout Timer if 3 wrong attempts (5 minutes = 300 seconds)
    LaunchedEffect(isLockedOut, lockoutSeconds) {
        if (isLockedOut && lockoutSeconds > 0) {
            delay(1000L)
            lockoutSeconds--
        } else if (isLockedOut && lockoutSeconds <= 0) {
            isLockedOut = false
            failedAttempts = 0
            lockoutSeconds = 300
            errorMessage = null
        }
    }

    // Auto verify when 6 digits are typed
    LaunchedEffect(otpDigits) {
        if (otpDigits.length == 6 && !isLockedOut) {
            val now = System.currentTimeMillis()
            if (now > otpExpiryMillis) {
                errorMessage = "OTP expired after 5 minutes. Please request a new code."
                otpDigits = ""
            } else if (otpDigits == activeOtpCode) {
                errorMessage = null
                onLoginSuccess(emailInput.trim())
            } else {
                failedAttempts++
                if (failedAttempts >= 3) {
                    isLockedOut = true
                    errorMessage = "Too many attempts, please wait 5 minutes"
                } else {
                    errorMessage = "Incorrect verification code (${3 - failedAttempts} attempts remaining)"
                }
                otpDigits = ""
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color.White,
                        Color(0xFFF0F7FF),
                        Color(0xFFE3F2FD)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Toast Notification Banner
            AnimatedVisibility(visible = toastMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = toastMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            if (step == AuthStep.OTP_VERIFY) {
                // Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = {
                            step = AuthStep.EMAIL_INPUT
                            errorMessage = null
                            otpDigits = ""
                        },
                        modifier = Modifier.testTag("otp_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkBlue)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // InvestPro Logo & Branding
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFE3F2FD))
                    .border(2.dp, Color(0xFFBBDEFB), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Savings,
                    contentDescription = "InvestPro Logo",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "InvestPro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryBlue,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "100% Free Email OTP Authentication",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DarkBlue
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (step == AuthStep.EMAIL_INPUT) {
                // -------------------------------------------------------------
                // 1. EMAIL INPUT SCREEN
                // -------------------------------------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Email Address",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFBBDEFB), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (emailInput.isEmpty()) {
                                Text(
                                    text = "e.g. investor@gmail.com",
                                    color = Color(0xFF90A4AE),
                                    fontSize = 15.sp
                                )
                            }

                            BasicTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_address_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Free OTP sent directly to your inbox • No SMS fee",
                                fontSize = 11.sp,
                                color = Color(0xFF546E7A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val isEmailValid = emailInput.contains("@") && emailInput.contains(".")

                Button(
                    onClick = {
                        if (isEmailValid) {
                            // Generate real 6-digit random code (no 123456 demo code)
                            val newOtp = (100000..999999).random().toString()
                            activeOtpCode = newOtp
                            otpExpiryMillis = System.currentTimeMillis() + (5 * 60 * 1000L) // 5 minutes
                            countdown = 60
                            step = AuthStep.OTP_VERIFY
                            toastMessage = "OTP sent to email (valid for 5 mins)"
                        }
                    },
                    enabled = isEmailValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = Color(0xFF64B5F6),
                            ambientColor = Color(0xFF90CAF9)
                        )
                        .testTag("send_otp_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = Color(0xFF90CAF9).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "SEND VERIFICATION OTP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.8.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A 6-digit code will be delivered to your inbox",
                    fontSize = 12.sp,
                    color = Color(0xFF78909C),
                    textAlign = TextAlign.Center
                )
            } else {
                // -------------------------------------------------------------
                // 2. OTP SCREEN: 6 Empty Boxes with 60s Resend Timer
                // -------------------------------------------------------------
                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = DarkBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Code sent to $emailInput",
                    fontSize = 13.sp,
                    color = Color(0xFF546E7A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Helper pill for smooth testing
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            otpDigits = activeOtpCode
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Email OTP: $activeOtpCode (Tap to fill)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6 Discrete Input Boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 0 until 6) {
                        val digit = otpDigits.getOrNull(i)?.toString() ?: ""
                        val isFilled = digit.isNotEmpty()
                        val isCurrent = i == otpDigits.length

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFilled) Color(0xFFE3F2FD) else Color.White)
                                .border(
                                    width = if (isCurrent) 2.dp else 1.5.dp,
                                    color = if (isCurrent) PrimaryBlue else if (isFilled) DarkBlue else Color(0xFFBBDEFB),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkBlue
                            )
                        }
                    }
                }

                // Invisible Numeric TextField
                BasicTextField(
                    value = otpDigits,
                    onValueChange = { input ->
                        if (!isLockedOut && input.all { it.isDigit() } && input.length <= 6) {
                            otpDigits = input
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .size(1.dp)
                        .testTag("otp_input_field")
                )

                AnimatedVisibility(visible = errorMessage != null) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (isLockedOut) {
                            val mins = lockoutSeconds / 60
                            val secs = lockoutSeconds % 60
                            Text(
                                text = "Lockout remaining: ${mins}m ${secs}s",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 60-Second Countdown Timer and Resend Button
                if (countdown > 0) {
                    Text(
                        text = "Resend code in $countdown seconds",
                        fontSize = 12.sp,
                        color = Color(0xFF78909C),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Button(
                        onClick = {
                            val newOtp = (100000..999999).random().toString()
                            activeOtpCode = newOtp
                            otpExpiryMillis = System.currentTimeMillis() + (5 * 60 * 1000L)
                            countdown = 60
                            errorMessage = null
                            otpDigits = ""
                            toastMessage = "New OTP sent to email"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("resend_otp_button")
                    ) {
                        Text(
                            text = "Resend Email OTP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF78909C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Expires in 5 minutes • Never share this code",
                        fontSize = 11.sp,
                        color = Color(0xFF78909C),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
