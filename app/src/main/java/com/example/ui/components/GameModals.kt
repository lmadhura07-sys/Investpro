package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.localization.AppStrings
import com.example.data.model.GameType
import com.example.data.model.PaymentMethod
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ----------------------------------------------------
// 1. SPIN & WIN WHEEL DIALOG
// ----------------------------------------------------
@Composable
fun SpinAndWinDialog(
    isFreePlay: Boolean,
    onDismiss: () -> Unit,
    onSpinClaim: (reward: Double) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var selectedReward by remember { mutableStateOf<Double?>(null) }

    // 6 Wheel sectors: prize and color
    val sectors = remember {
        listOf(
            1000.0 to Color(0xFFFFB300), // Gold
            50.0 to Color(0xFF1976D2),   // Blue
            500.0 to Color(0xFF7B1FA2),  // Purple
            100.0 to Color(0xFF00C853),  // Green
            200.0 to Color(0xFFFF5722),  // Deep Orange
            20.0 to Color(0xFF0288D1)    // Cyan
        )
    }

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_spin_and_win"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎡 SPIN & WIN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Text(
                            text = if (isFreePlay) "🎁 Free Play (No Cost)" else "Entry: Rs. 100 from Main Wallet",
                            fontSize = 12.sp,
                            color = if (isFreePlay) SuccessGreen else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSpinning,
                        modifier = Modifier.testTag("close_spin_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wheel Container with Pointer
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .testTag("wheel_canvas_box"),
                    contentAlignment = Alignment.Center
                ) {
                    // Wheel Canvas
                    Canvas(
                        modifier = Modifier
                            .size(210.dp)
                            .rotate(rotation.value)
                    ) {
                        val canvasRadius = size.minDimension / 2
                        val sweep = 360f / sectors.size

                        sectors.forEachIndexed { index, (reward, color) ->
                            val startAngle = index * sweep
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true
                            )
                        }

                        // Outer border ring
                        drawCircle(
                            color = DarkBlue,
                            radius = canvasRadius,
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }

                    // Center Hub Button
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(DarkBlue)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WIN\n1000",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }

                    // Arrow indicator at the top
                    Canvas(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height)
                            lineTo(0f, 0f)
                            lineTo(size.width, 0f)
                            close()
                        }
                        drawPath(path, color = Color(0xFFD32F2F))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sector Legends
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    sectors.take(3).forEach { (amt, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rs.${amt.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    sectors.drop(3).forEach { (amt, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rs.${amt.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedReward != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎉 YOU WON Rs. ${selectedReward?.toInt()}!",
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Credited to your Game Wallet balance",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (!isSpinning) {
                            coroutineScope.launch {
                                isSpinning = true
                                selectedReward = null
                                // Choose random outcome
                                val targetSectorIndex = Random.nextInt(sectors.size)
                                val sweep = 360f / sectors.size
                                // Top pointer points to 270 degrees in coordinate space
                                val targetSectorCenter = (targetSectorIndex * sweep) + (sweep / 2)
                                val finalAngle = (360f * 5) + (270f - targetSectorCenter)

                                rotation.animateTo(
                                    targetValue = finalAngle,
                                    animationSpec = tween(durationMillis = 3200, easing = FastOutSlowInEasing)
                                )
                                val prize = sectors[targetSectorIndex].first
                                selectedReward = prize
                                onSpinClaim(prize)
                                isSpinning = false
                            }
                        }
                    },
                    enabled = !isSpinning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_spin_wheel"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isSpinning) "Spinning Wheel..." else if (selectedReward != null) "SPIN AGAIN" else "SPIN NOW!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. TAP TAP COIN DIALOG
// ----------------------------------------------------
@Composable
fun TapTapCoinDialog(
    isFreePlay: Boolean,
    onDismiss: () -> Unit,
    onComplete: (taps: Int) -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(10) }
    var tapCount by remember { mutableIntStateOf(0) }
    var coinScale by remember { mutableFloatStateOf(1f) }
    var gameFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            secondsLeft = 10
            tapCount = 0
            gameFinished = false
            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft -= 1
            }
            isPlaying = false
            gameFinished = true
            onComplete(tapCount)
        }
    }

    Dialog(onDismissRequest = { if (!isPlaying) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_tap_tap_coin"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🪙 TAP TAP COIN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Text(
                            text = "Reach 20 taps in 10s to win Rs. 50!",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isPlaying,
                        modifier = Modifier.testTag("close_tap_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer & Score Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${secondsLeft}s Left", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 14.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$tapCount Taps", fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress towards 20
                LinearProgressIndicator(
                    progress = { (tapCount / 20f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SuccessGreen,
                    trackColor = Color(0xFFE0E0E0)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Giant Animated Tap Coin Button
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(coinScale)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFEE58), Color(0xFFFFB300), Color(0xFFF57F17))
                            )
                        )
                        .border(4.dp, Color(0xFFFFF9C4), CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (isPlaying) {
                                tapCount += 1
                                coinScale = 0.88f
                            }
                        }
                        .testTag("tap_coin_target"),
                    contentAlignment = Alignment.Center
                ) {
                    LaunchedEffect(coinScale) {
                        if (coinScale < 1f) {
                            delay(50L)
                            coinScale = 1f
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFF795548),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isPlaying) "TAP!" else "Rs. 50",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF4E342E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (gameFinished) {
                    val won = tapCount >= 20
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (won) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (won) "🎉 SUCCESS! You scored $tapCount taps and won Rs. 50!" else "Need 20 taps to win. You got $tapCount taps.",
                            fontWeight = FontWeight.Bold,
                            color = if (won) SuccessGreen else Color(0xFFC62828),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Button(
                    onClick = {
                        isPlaying = true
                    },
                    enabled = !isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_start_tap"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isPlaying) "TAP THE COIN RAPIDLY!" else if (gameFinished) "TRY AGAIN" else "START 10s CHALLENGE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3. NUMBER GUESS DIALOG (1 - 10)
// ----------------------------------------------------
@Composable
fun NumberGuessDialog(
    onDismiss: () -> Unit,
    onSubmitGuess: (number: Int) -> Unit
) {
    var selectedNumber by remember { mutableIntStateOf(7) }
    var isSubmitted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_number_guess"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎲 NUMBER GUESS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Text(
                            text = "Guess 1 to 10 • Win 5X (Rs. 500) • Entry Rs. 100",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_guess_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pick your lucky number:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2 rows of 5 numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..5).forEach { num ->
                        val isSelected = selectedNumber == num
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryBlue else Color(0xFFF5F9FF))
                                .border(1.5.dp, if (isSelected) DarkBlue else Color(0xFFBBDEFB), CircleShape)
                                .clickable { selectedNumber = num }
                                .testTag("guess_num_$num"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$num",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else DarkBlue,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (6..10).forEach { num ->
                        val isSelected = selectedNumber == num
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryBlue else Color(0xFFF5F9FF))
                                .border(1.5.dp, if (isSelected) DarkBlue else Color(0xFFBBDEFB), CircleShape)
                                .clickable { selectedNumber = num }
                                .testTag("guess_num_$num"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$num",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else DarkBlue,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Exact Match: Rs. 500 (5X Win)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                            Text("Off by 1: Rs. 100 Consolation Payout", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isSubmitted = true
                        onSubmitGuess(selectedNumber)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_submit_guess"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "LOCK IN NUMBER $selectedNumber (Rs. 100)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. TRANSFER TO MAIN WALLET MODAL
// ----------------------------------------------------
@Composable
fun TransferGameModal(
    gameWalletBalance: Double,
    onDismiss: () -> Unit,
    onConfirmTransfer: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf(if (gameWalletBalance >= 200) "200" else gameWalletBalance.toInt().toString()) }
    val isVip = gameWalletBalance >= 1000.0
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val vipBonus = if (isVip) amount * 0.05 else 0.0
    val totalToMain = amount + vipBonus

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_transfer_game_wallet"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Transfer to Main Wallet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Text(
                            text = "Move game wins to invest in 3/6/12M Plans",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_transfer_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balance summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available Game Balance:", fontSize = 12.sp, color = DarkBlue)
                        Text("Rs. ${"%,.2f".format(gameWalletBalance)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }

                if (isVip) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VIP Gamer (+5% Extra Bonus on Transfer!)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Transfer Amount (LKR)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_transfer_amount")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100, 200, 500).forEach { preset ->
                        OutlinedButton(
                            onClick = { amountText = preset.toString() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Rs.$preset", fontSize = 11.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = { amountText = gameWalletBalance.toInt().toString() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("MAX", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isVip && amount > 0) {
                    Text(
                        text = "Total to Main Wallet: Rs. ${"%,.2f".format(totalToMain)} (+Rs. ${"%,.2f".format(vipBonus)} VIP bonus)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        if (amount > 0 && amount <= gameWalletBalance) {
                            onConfirmTransfer(amount)
                        }
                    },
                    enabled = amount > 0 && amount <= gameWalletBalance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_transfer"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("TRANSFER TO MAIN WALLET", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. GAME WALLET WITHDRAWAL MODAL (Min Rs. 200)
// ----------------------------------------------------
@Composable
fun GameWithdrawModal(
    gameWalletBalance: Double,
    savedAccount: String,
    onDismiss: () -> Unit,
    onConfirmWithdrawal: (amount: Double, method: PaymentMethod, destination: String) -> Unit
) {
    var amountText by remember { mutableStateOf("200") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
    var destinationText by remember { mutableStateOf(savedAccount.ifBlank { "Commercial Bank - 8001234567" }) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = amount >= 200.0 && amount <= gameWalletBalance && destinationText.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_withdraw_game_wallet"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Withdraw Game Earnings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Text(
                            text = "Minimum withdrawal is Rs. 200",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_game_withdraw_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Available Game Cash:", fontSize = 12.sp, color = DarkBlue)
                        Text("Rs. ${"%,.2f".format(gameWalletBalance)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Withdraw Amount (Min Rs. 200)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amount > 0 && amount < 200.0,
                    supportingText = {
                        if (amount > 0 && amount < 200.0) {
                            Text("Minimum withdrawal is Rs. 200", color = Color.Red, fontSize = 10.sp)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_game_withdraw_amount")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Payout Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkBlue)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        PaymentMethod.BANK_TRANSFER to "Bank",
                        PaymentMethod.EZ_CASH to "eZ Cash",
                        PaymentMethod.LANKA_QR to "LankaQR",
                        PaymentMethod.USDT to "USDT"
                    ).forEach { (method, label) ->
                        val isSelected = selectedMethod == method
                        OutlinedButton(
                            onClick = { selectedMethod = method },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.Transparent
                            )
                        ) {
                            Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = destinationText,
                    onValueChange = { destinationText = it },
                    label = { Text("Account / Mobile / Wallet Address") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_game_withdraw_dest")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isValid) {
                            onConfirmWithdrawal(amount, selectedMethod, destinationText)
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_game_withdraw"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SUBMIT WITHDRAWAL (Rs. ${"%,.0f".format(amount)})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
