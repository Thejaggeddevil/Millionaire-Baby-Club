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
import com.example.babyparenting.ui.theme.LocalAppColors
import kotlin.math.atan2

@Composable
fun SectionHeader(group: AgeGroup, modifier: Modifier = Modifier) {
    val c      = LocalAppColors.current
    val accent = Color(group.accentColor)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(c.bgCard.copy(alpha = 0.85f))   // same as card for consistency
            .border(1.dp, accent.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(7.dp))
        Column {
            Text(group.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = c.cardTextPrimary)
            Text(group.description, fontSize = 9.sp, color = c.cardTextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun PathCanvas(nodePositions: List<Offset>, completedCount: Int = 0, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Canvas(modifier = modifier) {
        if (nodePositions.size < 2) return@Canvas
        val fullPath = Path()
        fullPath.moveTo(nodePositions[0].x, nodePositions[0].y)
        for (i in 0 until nodePositions.size - 1) {
            val cur = nodePositions[i]; val next = nodePositions[i + 1]
            val midY = (cur.y + next.y) / 2f
            fullPath.cubicTo(cur.x, midY, next.x, midY, next.x, next.y)
        }
        val measure  = PathMeasure().also { it.setPath(fullPath, false) }
        val fraction = (completedCount.toFloat() / (nodePositions.size - 1f)).coerceIn(0f, 1f)
        val splitAt  = measure.length * fraction

        drawPath(fullPath, c.pathCompleted.copy(alpha = 0.12f),
            style = Stroke(18f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(fullPath, c.pathFuture,
            style = Stroke(3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f))))

        if (fraction > 0f) {
            val donePath = Path()
            measure.getSegment(0f, splitAt, donePath, true)
            drawPath(donePath, c.pathCompleted, style = Stroke(5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        nodePositions.forEachIndexed { idx, pos ->
            val done = idx < completedCount
            drawCircle(if (done) c.pathCompleted else c.pathFuture, 10f, pos)
            // Inner dot: use bgCard so it shows against whatever the path color is
            drawCircle(if (done) Color.White else c.bgMain, 5.5f, pos)
        }
    }
}

@Composable
fun FootstepsLayer(nodePositions: List<Offset>, completedCount: Int, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    if (nodePositions.size < 2) return

    val transition = rememberInfiniteTransition(label = "marauder")
    val progress by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart), label = "wave")

    Canvas(modifier = modifier) {
        for (seg in 0 until nodePositions.size - 1) {
            when {
                seg < completedCount  -> drawStaticFootprints(nodePositions[seg], nodePositions[seg + 1], c.pathCompleted.copy(alpha = 0.55f))
                seg == completedCount -> drawAnimatedFootprints(nodePositions[seg], nodePositions[seg + 1], progress, c.coral)
                else                  -> drawStaticFootprints(nodePositions[seg], nodePositions[seg + 1], c.pathFuture.copy(alpha = 0.45f))
            }
        }
    }
}

private fun DrawScope.drawStaticFootprints(start: Offset, end: Offset, color: Color) {
    val ar = atan2((end.y - start.y), (end.x - start.x)); val ad = Math.toDegrees(ar.toDouble()).toFloat()
    val px = -kotlin.math.sin(ar).toFloat(); val py = kotlin.math.cos(ar).toFloat()
    for (i in 0 until 5) {
        val t = (i + 1f) / 6f; val sign = if (i % 2 == 0) 1f else -1f
        val cx = lerp(start.x, end.x, t) + px * 20f * sign; val cy = lerp(start.y, end.y, t) + py * 20f * sign
        rotate(ad + 90f, Offset(cx, cy)) { drawFootprint(Offset(cx, cy), 16f, color, i % 2 == 0) }
    }
}

private fun DrawScope.drawAnimatedFootprints(start: Offset, end: Offset, progress: Float, color: Color) {
    val ar = atan2((end.y - start.y), (end.x - start.x)); val ad = Math.toDegrees(ar.toDouble()).toFloat()
    val px = -kotlin.math.sin(ar).toFloat(); val py = kotlin.math.cos(ar).toFloat()
    for (i in 0 until 5) {
        val t = (i + 1f) / 6f; val alpha = waveAlpha(progress, t * 0.75f).coerceIn(0f, 1f)
        if (alpha <= 0f) continue
        val sign = if (i % 2 == 0) 1f else -1f
        val cx = lerp(start.x, end.x, t) + px * 20f * sign; val cy = lerp(start.y, end.y, t) + py * 20f * sign
        drawCircle(color.copy(alpha = alpha * 0.18f), 26f, Offset(cx, cy))
        rotate(ad + 90f, Offset(cx, cy)) { drawFootprint(Offset(cx, cy), 16f, color.copy(alpha = alpha * 0.90f), i % 2 == 0) }
    }
}

private fun waveAlpha(p: Float, phase: Float): Float {
    val w = 0.35f; val s = ((p - phase) + 1f) % 1f
    return when { s < 0.06f -> s / 0.06f; s < w - 0.06f -> 1f; s < w -> 1f - (s - (w - 0.06f)) / 0.06f; else -> 0f }.coerceIn(0f, 1f)
}

private fun DrawScope.drawFootprint(center: Offset, size: Float, color: Color, right: Boolean) {
    val f = if (right) 1f else -1f
    drawCircle(color, size * 0.44f, Offset(center.x + f * size * 0.08f, center.y + size * 0.32f))
    drawCircle(color, size * 0.32f, Offset(center.x - f * size * 0.05f, center.y - size * 0.04f))
    listOf(Offset(-size*.30f,-size*.46f), Offset(-size*.10f,-size*.54f), Offset(size*.12f,-size*.50f), Offset(size*.30f,-size*.38f))
        .forEach { t -> drawCircle(color, size * 0.14f, Offset(center.x + f * t.x, center.y + t.y)) }
}

fun computeNodePositions(count: Int, canvasWidthPx: Float, canvasHeightPx: Float): List<Offset> {
    if (count == 0) return emptyList()
    val segH = canvasHeightPx / (count + 1).toFloat()
    return (0 until count).map { i -> Offset(if (i % 2 == 0) canvasWidthPx * 0.28f else canvasWidthPx * 0.72f, segH * (i + 1)) }
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t