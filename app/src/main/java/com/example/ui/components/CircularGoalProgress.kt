package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularGoalProgress(
    progressPercent: Float,
    currentSaved: Double,
    targetAmount: Double,
    monthsCompleted: Int,
    totalMonths: Int,
    modifier: Modifier = Modifier,
    canvasSize: Dp = 220.dp,
    strokeWidth: Dp = 16.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (progressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier.size(canvasSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(canvasSize)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.width - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val radius = arcSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background Track
            drawArc(
                color = Color(0xFFE3F2FD),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated Progress Arc
            val sweepAngle = 360f * animatedProgress
            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to PrimaryBlue,
                        0.7f to PrimaryBlue,
                        1.0f to SuccessGreen
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Month Markers along the circle (10 Months)
            for (i in 1..totalMonths) {
                val angleDeg = -90f + (360f / totalMonths) * i
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val dotX = (center.x + (radius) * cos(angleRad)).toFloat()
                val dotY = (center.y + (radius) * sin(angleRad)).toFloat()

                val isPassed = i <= monthsCompleted
                drawCircle(
                    color = if (isPassed) SuccessGreen else Color.White,
                    radius = if (isPassed) 4.dp.toPx() else 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                if (!isPassed) {
                    drawCircle(
                        color = Color(0xFF90CAF9),
                        radius = 3.dp.toPx(),
                        center = Offset(dotX, dotY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        // Inner Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = DarkBlue
            )
            Text(
                text = "Month $monthsCompleted of $totalMonths",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rs. ${"%,.0f".format(currentSaved)} / ${"%,.0f".format(targetAmount)}",
                fontSize = 11.sp,
                color = Color(0xFF546E7A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
