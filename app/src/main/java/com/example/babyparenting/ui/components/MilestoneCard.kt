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
import com.example.babyparenting.ui.theme.AppColors
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
        if (isLocked) 0.45f else 1f, tween(350), label = "l")

    val trueAccent  = Color(milestone.accentColor)
    val accentColor = if (isLocked) AppColors.Locked else trueAccent
    val isDone      = milestone.isCompleted
    val shape       = RoundedCornerShape(14.dp)

    val cardBg = when {
        isLocked -> AppColors.BgSurface.copy(alpha = 0.6f)
        isDone   -> AppColors.BgSurface
        else     -> AppColors.BgSurface
    }
    val borderColor = when {
        isLocked -> AppColors.Border.copy(alpha = 0.5f)
        isDone   -> trueAccent.copy(alpha = 0.55f)
        else     -> AppColors.Border
    }

    Column(
        modifier = modifier
            .scale(entranceScale * pressScale)
            .alpha(entranceAlpha * lockedAlpha)
            .shadow(
                elevation    = if (isLocked) 1.dp else if (isDone) 4.dp else 6.dp,
                shape        = shape,
                ambientColor = accentColor.copy(alpha = 0.20f),
                spotColor    = accentColor.copy(alpha = 0.20f)
            )
            .clip(shape)
            .background(cardBg)
            .border(if (isDone && !isLocked) 1.5.dp else 1.dp, borderColor, shape)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        // Source badge
        Box(
            Modifier.fillMaxWidth()
                .background(if (isLocked) AppColors.Border.copy(alpha = 0.4f) else accentColor.copy(alpha = 0.18f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                if (isLocked) "🔒 Locked" else "${milestone.source.emoji} ${milestone.source.displayName}",
                fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                color    = if (isLocked) AppColors.TextMuted else accentColor
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(26.dp).clip(CircleShape)
                    .background(when {
                        isLocked -> AppColors.Locked
                        isDone   -> trueAccent
                        else     -> trueAccent.copy(alpha = 0.20f)
                    })
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        enabled           = !isLocked && !isDone
                    ) { onToggleCompletion() }
            ) {
                when {
                    isLocked -> Icon(Icons.Default.Lock, null, tint = AppColors.TextMuted, modifier = Modifier.size(13.dp))
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
                        isLocked -> AppColors.TextMuted
                        isDone   -> trueAccent
                        else     -> AppColors.TextPrimary
                    },
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    if (isLocked) "Finish previous step first" else milestone.subtitle,
                    fontSize = 10.sp,
                    color    = if (isLocked) AppColors.TextMuted else AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                if (isLocked) Icons.Default.Lock else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint     = if (isDone) trueAccent else AppColors.TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}