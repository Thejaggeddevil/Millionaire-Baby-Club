package com.example.babyparenting.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.Milestone
import com.example.babyparenting.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun MilestoneCard(
    milestone: Milestone,
    index: Int,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 60L + 150L); visible = true }

    val entranceScale by animateFloatAsState(
        if (visible) 1f else 0.5f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "s")
    val entranceAlpha by animateFloatAsState(
        if (visible) 1f else 0f, spring(stiffness = Spring.StiffnessMedium), label = "a")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        if (pressed && !isLocked) 0.95f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "p")
    val lockedAlpha by animateFloatAsState(
        if (isLocked) 0.50f else 1f, tween(350), label = "l")

    val trueAccent = Color(milestone.accentColor)
    val isDone     = milestone.isCompleted
    val shape      = RoundedCornerShape(14.dp)

    // ── KEY: card bg is OPPOSITE of screen bg for contrast ───────────────────
    // Light theme → bgCard = dark purple  → cardTextPrimary = warm white
    // Dark theme  → bgCard = cream white  → cardTextPrimary = dark navy
    val cardBg = when {
        isLocked -> c.bgCardLocked
        else     -> c.bgCard
    }
    val borderColor = when {
        isLocked -> c.cardBorder.copy(alpha = 0.4f)
        isDone   -> c.cardBorderActive   // coral accent on completed
        else     -> c.cardBorder
    }

    Column(
        modifier = modifier
            .scale(entranceScale * pressScale)
            .alpha(entranceAlpha * lockedAlpha)
            .shadow(
                elevation    = if (isLocked) 1.dp else if (isDone) 5.dp else 8.dp,
                shape        = shape,
                ambientColor = if (isDone) trueAccent.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.20f),
                spotColor    = if (isDone) trueAccent.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.20f)
            )
            .clip(shape)
            .background(cardBg)
            .border(if (isDone && !isLocked) 1.5.dp else 1.dp, borderColor, shape)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        // Source badge — slightly lighter/darker than card bg
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(if (isDone) trueAccent.copy(alpha = 0.22f)
                else if (isLocked) c.cardBorder.copy(alpha = 0.3f)
                else trueAccent.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                if (isLocked) "🔒 Locked" else "${milestone.source.emoji} ${milestone.source.displayName}",
                fontSize   = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isLocked) c.cardTextSecondary else trueAccent
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Number circle / check / lock
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp).clip(CircleShape)
                    .background(when {
                        isLocked -> c.cardBorder
                        isDone   -> trueAccent
                        else     -> trueAccent.copy(alpha = 0.22f)
                    })
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        enabled           = !isLocked && !isDone
                    ) { onToggleCompletion() }
            ) {
                when {
                    isLocked -> Icon(Icons.Default.Lock, null, tint = c.cardTextSecondary, modifier = Modifier.size(13.dp))
                    isDone   -> Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    else     -> Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = trueAccent)
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    if (isLocked) "Complete step ${index}" else milestone.title,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = when {
                        isLocked -> c.cardTextSecondary
                        isDone   -> trueAccent
                        else     -> c.cardTextPrimary    // ← contrast text
                    },
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    if (isLocked) "Finish previous step first" else milestone.subtitle,
                    fontSize = 10.sp,
                    color    = c.cardTextSecondary,      // ← contrast secondary
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                if (isLocked) Icons.Default.Lock else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint     = if (isDone) trueAccent else c.cardTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}