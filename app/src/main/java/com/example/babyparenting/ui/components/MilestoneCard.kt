package com.example.babyparenting.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import kotlinx.coroutines.delay

@Composable
fun MilestoneCard(
    milestone: Milestone,
    index: Int,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L + 150L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.5f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "scale$index"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "alpha$index"
    )

    // Press scale
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "press$index"
    )

    val accentColor = Color(milestone.accentColor)
    val isDone      = milestone.isCompleted
    val shape       = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .scale(scale * pressScale)
            .alpha(alpha)
            .shadow(
                elevation    = if (isDone) 3.dp else 6.dp,
                shape        = shape,
                ambientColor = accentColor.copy(alpha = 0.15f),
                spotColor    = accentColor.copy(alpha = 0.20f)
            )
            .clip(shape)
            .background(if (isDone) accentColor.copy(alpha = 0.09f) else Color.White)
            .border(
                width = if (isDone) 1.5.dp else 1.dp,
                color = if (isDone) accentColor.copy(alpha = 0.45f) else Color(0xFFEEEEEE),
                shape = shape
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        // Source badge at top of card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text       = "${milestone.source.emoji} ${milestone.source.displayName}",
                fontSize   = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color      = accentColor
            )
        }

        // Card body
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Numbered circle / check — tap to toggle completion
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isDone) accentColor else accentColor.copy(alpha = 0.14f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onToggleCompletion() }
            ) {
                if (isDone) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text       = "${index + 1}",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text       = milestone.title,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isDone) accentColor else Color(0xFF1A1A2E),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text     = milestone.subtitle,
                    fontSize = 10.sp,
                    color    = Color(0xFF9E9E9E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint               = if (isDone) accentColor else Color(0xFFBDBDBD),
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}
