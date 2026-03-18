package com.example.babyparenting.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.AgeGroup
import kotlin.math.atan2

// ── Section Header ─────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(group: AgeGroup, modifier: Modifier = Modifier) {
    val accent = Color(group.accentColor)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(Modifier.width(7.dp))
        Column {
            Text(
                text       = group.label,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = accent
            )
            Text(
                text     = group.description,
                fontSize = 9.sp,
                color    = Color(0xFF777777),
                maxLines = 1
            )
        }
    }
}

// ── PathCanvas ────────────────────────────────────────────────────────────────

/**
 * Draws the S-shaped journey path.
 * Completed segment = solid dark blue.
 * Remaining segment = dashed light blue.
 * Node dots show completion state.
 */
@Composable
fun PathCanvas(
    nodePositions: List<Offset>,
    completedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (nodePositions.size < 2) return@Canvas

        // Build full bezier path through all nodes
        val fullPath = Path()
        fullPath.moveTo(nodePositions[0].x, nodePositions[0].y)
        for (i in 0 until nodePositions.size - 1) {
            val cur  = nodePositions[i]
            val next = nodePositions[i + 1]
            val midY = (cur.y + next.y) / 2f
            fullPath.cubicTo(cur.x, midY, next.x, midY, next.x, next.y)
        }

        // Measure for completed/remaining split
        val measure = PathMeasure()
        measure.setPath(fullPath, false)
        val totalLen  = measure.length
        val fraction  = if (nodePositions.size > 1)
            (completedCount.toFloat() / (nodePositions.size - 1f)).coerceIn(0f, 1f) else 0f
        val splitAt   = totalLen * fraction

        // Glow behind entire path
        drawPath(
            fullPath,
            Color(0xFFBBDEFB).copy(alpha = 0.28f),
            style = Stroke(20f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Future dashed light blue
        drawPath(
            fullPath,
            Color(0xFF90CAF9),
            style = Stroke(
                width      = 3.5f,
                cap        = StrokeCap.Round,
                join       = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f), 0f)
            )
        )

        // Completed solid blue
        if (fraction > 0f) {
            val donePath = Path()
            measure.getSegment(0f, splitAt, donePath, true)
            drawPath(
                donePath,
                Color(0xFF1565C0),
                style = Stroke(5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Node dots
        nodePositions.forEachIndexed { idx, pos ->
            val done = idx < completedCount
            drawCircle(if (done) Color(0xFF1565C0) else Color(0xFF90CAF9), 10f, pos)
            drawCircle(if (done) Color.White else Color(0xFFE3F2FD), 5.5f, pos)
        }
    }
}

// ── FootstepsLayer ────────────────────────────────────────────────────────────

/**
 * Marauder's Map style footprints.
 * Completed segments → static visible footprints (path already walked).
 * Active segment     → animated wave of appearing footprints.
 * Future segments    → faint ghost outline.
 */
@Composable
fun FootstepsLayer(
    nodePositions: List<Offset>,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    if (nodePositions.size < 2) return

    val transition = rememberInfiniteTransition(label = "marauder")
    val progress by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Canvas(modifier = modifier) {
        for (seg in 0 until nodePositions.size - 1) {
            when {
                seg < completedCount  -> drawStaticFootprints(
                    nodePositions[seg], nodePositions[seg + 1],
                    Color(0xFF1565C0).copy(alpha = 0.65f)
                )
                seg == completedCount -> drawAnimatedFootprints(
                    nodePositions[seg], nodePositions[seg + 1],
                    progress, Color(0xFF1976D2)
                )
                else                  -> drawStaticFootprints(
                    nodePositions[seg], nodePositions[seg + 1],
                    Color(0xFF90CAF9).copy(alpha = 0.20f)
                )
            }
        }
    }
}

// ── Static footprints ─────────────────────────────────────────────────────────

private fun DrawScope.drawStaticFootprints(start: Offset, end: Offset, color: Color) {
    val angleRad = atan2((end.y - start.y), (end.x - start.x))
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
    val perpX    = -kotlin.math.sin(angleRad).toFloat()
    val perpY    =  kotlin.math.cos(angleRad).toFloat()
    for (i in 0 until 5) {
        val t    = (i + 1f) / 6f
        val sign = if (i % 2 == 0) 1f else -1f
        val cx   = lerp(start.x, end.x, t) + perpX * 20f * sign
        val cy   = lerp(start.y, end.y, t) + perpY * 20f * sign
        rotate(angleDeg + 90f, Offset(cx, cy)) {
            drawFootprint(Offset(cx, cy), 16f, color, i % 2 == 0)
        }
    }
}

// ── Animated footprints ───────────────────────────────────────────────────────

private fun DrawScope.drawAnimatedFootprints(
    start: Offset,
    end: Offset,
    progress: Float,
    color: Color
) {
    val angleRad = atan2((end.y - start.y), (end.x - start.x))
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
    val perpX    = -kotlin.math.sin(angleRad).toFloat()
    val perpY    =  kotlin.math.cos(angleRad).toFloat()
    for (i in 0 until 5) {
        val t     = (i + 1f) / 6f
        val alpha = waveAlpha(progress, t * 0.75f)
        if (alpha <= 0f) continue
        val sign = if (i % 2 == 0) 1f else -1f
        val cx   = lerp(start.x, end.x, t) + perpX * 20f * sign
        val cy   = lerp(start.y, end.y, t) + perpY * 20f * sign
        // Glow ring
        drawCircle(color.copy(alpha = alpha * 0.20f), 26f, Offset(cx, cy))
        rotate(angleDeg + 90f, Offset(cx, cy)) {
            drawFootprint(Offset(cx, cy), 16f, color.copy(alpha = alpha * 0.90f), i % 2 == 0)
        }
    }
}

private fun waveAlpha(progress: Float, phase: Float): Float {
    val w       = 0.35f
    val shifted = ((progress - phase) + 1f) % 1f
    return when {
        shifted < 0.06f         -> shifted / 0.06f
        shifted < w - 0.06f    -> 1f
        shifted < w            -> 1f - (shifted - (w - 0.06f)) / 0.06f
        else                   -> 0f
    }.coerceIn(0f, 1f)
}

// ── Single footprint shape ────────────────────────────────────────────────────

private fun DrawScope.drawFootprint(
    center: Offset,
    size: Float,
    color: Color,
    isRight: Boolean
) {
    val f = if (isRight) 1f else -1f
    // Heel
    drawCircle(color, size * 0.44f, Offset(center.x + f * size * 0.08f, center.y + size * 0.32f))
    // Ball
    drawCircle(color, size * 0.32f, Offset(center.x - f * size * 0.05f, center.y - size * 0.04f))
    // 4 toes
    listOf(
        Offset(-size * 0.30f, -size * 0.46f),
        Offset(-size * 0.10f, -size * 0.54f),
        Offset( size * 0.12f, -size * 0.50f),
        Offset( size * 0.30f, -size * 0.38f)
    ).forEach { t ->
        drawCircle(color, size * 0.14f, Offset(center.x + f * t.x, center.y + t.y))
    }
}

// ── Node positions ────────────────────────────────────────────────────────────

/**
 * Computes pixel positions for [count] nodes, alternating
 * left 28% / right 72% inside the canvas, evenly spaced vertically.
 */
fun computeNodePositions(
    count: Int,
    canvasWidthPx: Float,
    canvasHeightPx: Float
): List<Offset> {
    if (count == 0) return emptyList()
    val segH = canvasHeightPx / (count + 1).toFloat()
    return (0 until count).map { i ->
        Offset(
            x = if (i % 2 == 0) canvasWidthPx * 0.28f else canvasWidthPx * 0.72f,
            y = segH * (i + 1)
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
