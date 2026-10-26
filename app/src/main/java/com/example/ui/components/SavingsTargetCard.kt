package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BonusGold
import com.example.ui.theme.BonusGoldLight
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import kotlin.math.cos
import kotlin.math.sin

enum class ChartDisplayMode {
    PROGRESS_RING,
    BAR_CHART
}

/**
 * Visual progress ring and bar chart component for tracking savings towards a Rs. 5,000 target.
 * Supports D3 / Recharts style radial donut arcs, animated milestone bar charts,
 * multi-tier milestone badges, and quick fund addition.
 */
@Composable
fun SavingsTargetCard(
    currentSavings: Double,
    initialTarget: Double = 5000.0,
    onDepositClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf(ChartDisplayMode.PROGRESS_RING) }
    var targetAmount by remember { mutableDoubleStateOf(initialTarget) }

    val rawProgress = if (targetAmount > 0.0) (currentSavings / targetAmount).toFloat() else 0f
    val progressFraction = rawProgress.coerceIn(0f, 1f)
    val progressPercent = (progressFraction * 100f).toInt()
    val isGoalAchieved = currentSavings >= targetAmount
    val remainingAmount = (targetAmount - currentSavings).coerceAtLeast(0.0)

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "SavingsProgressAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("savings_target_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGoalAchieved) SuccessGreenLight else Color(0xFFE3F2FD)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGoalAchieved) Icons.Default.EmojiEvents else Icons.Default.Savings,
                            contentDescription = "Savings Target",
                            tint = if (isGoalAchieved) SuccessGreen else PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Savings Target",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isGoalAchieved) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACHIEVED 🏆",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Target: Rs. ${"%,.0f".format(targetAmount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }

                // Chart View Mode Selector (Ring ⭕ vs Bar 📊)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (chartMode == ChartDisplayMode.PROGRESS_RING) PrimaryBlue else Color.Transparent
                            )
                            .clickable { chartMode = ChartDisplayMode.PROGRESS_RING }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("savings_chart_toggle_ring"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = "Progress Ring View",
                            tint = if (chartMode == ChartDisplayMode.PROGRESS_RING) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (chartMode == ChartDisplayMode.BAR_CHART) PrimaryBlue else Color.Transparent
                            )
                            .clickable { chartMode = ChartDisplayMode.BAR_CHART }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("savings_chart_toggle_bar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart View",
                            tint = if (chartMode == ChartDisplayMode.BAR_CHART) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Visualizer: Ring or Bar Chart
            when (chartMode) {
                ChartDisplayMode.PROGRESS_RING -> {
                    ProgressRingVisual(
                        animatedProgress = animatedProgress,
                        currentAmount = currentSavings,
                        targetAmount = targetAmount,
                        progressPercent = progressPercent,
                        isGoalAchieved = isGoalAchieved
                    )
                }
                ChartDisplayMode.BAR_CHART -> {
                    MilestoneBarChartVisual(
                        currentAmount = currentSavings,
                        targetAmount = targetAmount,
                        animatedProgress = animatedProgress
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric Summary Pills (Saved | Remaining | Target)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Current Saved",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rs. ${"%,.0f".format(currentSavings)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalAchieved) SuccessGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isGoalAchieved) "Status" else "Remaining",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isGoalAchieved) "Completed 🎉" else "Rs. ${"%,.0f".format(remainingAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalAchieved) SuccessGreen else Color(0xFFE65100)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target Goal",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rs. ${"%,.0f".format(targetAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Target Goal Selector: Rs. 5,000 (standard), Rs. 10,000, Rs. 25,000
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal Target:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(5000.0, 10000.0, 25000.0).forEach { preset ->
                    val isSelected = targetAmount == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { targetAmount = preset }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("savings_target_chip_${preset.toInt()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rs. ${"%,.0f".format(preset)}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Add Funds / Deposit CTA
            Button(
                onClick = onDepositClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("savings_add_funds_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGoalAchieved) SuccessGreen else PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isGoalAchieved) Icons.Default.CheckCircle else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGoalAchieved) "Target Reached! Keep Saving" else "Add Funds Towards Rs. ${"%,.0f".format(targetAmount)} Target",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * High-performance D3/Recharts-inspired Radial Progress Ring drawn on Canvas.
 */
@Composable
private fun ProgressRingVisual(
    animatedProgress: Float,
    currentAmount: Double,
    targetAmount: Double,
    progressPercent: Int,
    isGoalAchieved: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("savings_progress_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(190.dp)) {
            val strokeWidthPx = 16.dp.toPx()
            val arcSize = size.width - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = arcSize / 2f

            // 1. Background Track (Subtle circular rail)
            drawArc(
                color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 2. Animated Gradient Arc (D3/Recharts aesthetic)
            val sweepAngle = (360f * animatedProgress).coerceIn(0f, 360f)
            if (sweepAngle > 0f) {
                val gradientBrush = Brush.sweepGradient(
                    0.0f to Color(0xFF00E5FF),
                    0.4f to PrimaryBlue,
                    0.8f to SuccessGreen,
                    1.0f to if (isGoalAchieved) BonusGold else SuccessGreen
                )
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }

            // 3. Milestone Markers at 25%, 50%, 75%, 100%
            val milestones = listOf(0.25f, 0.50f, 0.75f, 1.0f)
            for (milestone in milestones) {
                val angleDeg = -90f + (360f * milestone)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val dotX = (center.x + radius * cos(angleRad)).toFloat()
                val dotY = (center.y + radius * sin(angleRad)).toFloat()
                val isReached = animatedProgress >= milestone

                // Outer border dot
                drawCircle(
                    color = if (isReached) SuccessGreen else Color.White,
                    radius = if (isReached) 4.5.dp.toPx() else 3.5.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                if (!isReached) {
                    drawCircle(
                        color = Color(0xFF90CAF9),
                        radius = 3.5.dp.toPx(),
                        center = Offset(dotX, dotY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        // Center Content Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$progressPercent%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = if (isGoalAchieved) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isGoalAchieved) "Target Reached! 🎉" else "Towards Rs. ${"%,.0f".format(targetAmount)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Rs. ${"%,.0f".format(currentAmount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Recharts-inspired Tiered Milestone Bar Chart component.
 * Displays 4 milestone columns towards the Rs. 5,000 savings target:
 * Tier 1 (25% - Rs. 1,250), Tier 2 (50% - Rs. 2,500), Tier 3 (75% - Rs. 3,750), Tier 4 (100% - Rs. 5,000).
 */
@Composable
private fun MilestoneBarChartVisual(
    currentAmount: Double,
    targetAmount: Double,
    animatedProgress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_bar_chart"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val tiers = listOf(
            MilestoneTier(title = "Starter", fraction = 0.25f, label = "25%"),
            MilestoneTier(title = "Halfway", fraction = 0.50f, label = "50%"),
            MilestoneTier(title = "Surge", fraction = 0.75f, label = "75%"),
            MilestoneTier(title = "Champion", fraction = 1.00f, label = "100%")
        )

        // 4 Vertical Bar Columns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            tiers.forEach { tier ->
                val tierTarget = targetAmount * tier.fraction
                val isReached = currentAmount >= tierTarget
                // Calculate fill fraction within this tier segment
                val tierProgress = (currentAmount / tierTarget).coerceIn(0.0, 1.0).toFloat()

                val animatedTierFill by animateFloatAsState(
                    targetValue = tierProgress,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "TierFill_${tier.title}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(68.dp)
                ) {
                    // Badge at top of column
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isReached) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isReached) "✓ ${tier.label}" else tier.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReached) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Vertical Bar Track
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedTierFill)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isReached) {
                                            listOf(Color(0xFF00E5FF), SuccessGreen)
                                        } else {
                                            listOf(PrimaryBlue, Color(0xFF1565C0))
                                        }
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Rs. ${"%,.0f".format(tierTarget)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReached) SuccessGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tier.title,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal connecting master progress track
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = SuccessGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

private data class MilestoneTier(
    val title: String,
    val fraction: Float,
    val label: String
)
