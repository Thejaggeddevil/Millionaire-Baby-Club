package com.example.babyparenting.ui.screens.millionaire

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.Activity
import com.example.babyparenting.ui.theme.AppColorScheme
import com.example.babyparenting.ui.theme.LocalAppColors
import com.example.babyparenting.ui.viewmodel.ActivitiesUiState
import com.example.babyparenting.ui.viewmodel.MillionaireViewModel

// ─────────────────────────────────────────────────────────────────────────────
// STRATEGY DETAIL SCREEN
//
// ✅ Activities 1–10 shown as a journey:
//    - #1 always unlocked
//    - #N locked until #N-1 is completed
//    - Next locked activities are dimmed but visible
//    - Completed ones show ✓ badge + coral border
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyDetailScreen(
    strategyId: Int,
    strategyTitle: String,
    viewModel: MillionaireViewModel,
    onActivityClick: (activityId: Int, strategyId: Int) -> Unit,
    onBackClick: () -> Unit
) {
    val activitiesState     by viewModel.activitiesState.collectAsState()
    val completedActivities by viewModel.completedActivities.collectAsState()
    val childAge            by viewModel.childAge.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(strategyId) {
        viewModel.loadActivitiesForStrategy(strategyId)
        val userId = com.example.babyparenting.data.local.UserManager.getUserId(
            viewModel.getContext()
        )
        viewModel.loadCompletedActivities(userId)  // ✅ Har baar completed IDs reload karo
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
    ) {
        // ── Top Bar ──────────────────────────────────────────────────────────
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = strategyTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Activities",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary.copy(alpha = 0.5f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.bgSurface
            )
        )

        when (val state = activitiesState) {

            is ActivitiesUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colors.coral, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Loading activities...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            is ActivitiesUiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            state.message,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.loadActivitiesForStrategy(strategyId) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.coral),
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Retry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            is ActivitiesUiState.Success -> {
                // Filter + sort
                val activities = state.activities
                    .filter { activity ->
                        if (childAge == 0) true
                        else {
                            val min = activity.age_min ?: 0
                            val max = activity.age_max ?: 999
                            (childAge / 12) in min..max
                        }
                    }
                    .sortedBy { it.level ?: 1 }

                if (activities.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👶", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No activities for this age group",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Header summary
                    val completedCount = activities.count { completedActivities.contains(it.id) }

                    ActivitiesContent(
                        activities          = activities,
                        completedActivities = completedActivities,
                        completedCount      = completedCount,
                        strategyId          = strategyId,
                        colors              = colors,
                        onActivityClick     = onActivityClick
                    )
                }
            }

            else -> {}
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACTIVITIES CONTENT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActivitiesContent(
    activities: List<Activity>,
    completedActivities: Set<Int>,
    completedCount: Int,
    strategyId: Int,
    colors: AppColorScheme,
    onActivityClick: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // ── Header ──────────────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.coral.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, colors.coral.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Your Journey",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary.copy(alpha = 0.6f)
                        )
                        Text(
                            "$completedCount of ${activities.size} completed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.coral
                        )
                    }
                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / activities.size },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .padding(start = 16.dp),
                        color = colors.coral,
                        trackColor = colors.textPrimary.copy(alpha = 0.1f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        // ── Activities ──────────────────────────────────────────────────────
        itemsIndexed(activities) { index, activity ->
            val isCompleted = completedActivities.contains(activity.id)
            val isLocked = index > 0 && !completedActivities.contains(activities[index - 1].id)
            val isNext = index > 0 && !isLocked && !isCompleted
            val isFirst = index == 0
            val isLast = index == activities.size - 1

            ActivityJourneyItem(
                activity    = activity,
                index       = index,
                isCompleted = isCompleted,
                isLocked    = isLocked,
                isNext      = isNext,
                isFirst     = isFirst,
                isLast      = isLast,
                colors      = colors,
                onClick     = { onActivityClick(activity.id!!, strategyId) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACTIVITY JOURNEY ITEM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActivityJourneyItem(
    activity: Activity,
    index: Int,
    isCompleted: Boolean,
    isLocked: Boolean,
    isNext: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    colors: AppColorScheme,
    onClick: () -> Unit
) {
    val alpha = when {
        isCompleted -> 1f
        isNext      -> 1f
        isLocked    -> 0.6f
        else        -> 1f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!isLast) 0.dp else 0.dp)
            .alpha(alpha),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Left: Circle + Line ──────────────────────────────────────────────
        Column(
            modifier = Modifier.width(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Circle indicator
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> colors.coral
                            isNext      -> colors.coral.copy(alpha = 0.2f)
                            isLocked    -> colors.textPrimary.copy(alpha = 0.08f)
                            else        -> colors.bgSurface
                        }
                    )
                    .border(
                        width = if (isNext) 2.5.dp else 2.dp,
                        color = when {
                            isCompleted -> colors.coral
                            isNext      -> colors.coral
                            isLocked    -> colors.textPrimary.copy(alpha = 0.2f)
                            else        -> colors.textPrimary.copy(alpha = 0.15f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompleted -> Icon(
                        Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    else -> Text(
                        "${index + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isNext) colors.coral else colors.textPrimary.copy(alpha = 0.5f)
                    )
                }
            }

            // Vertical connector line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(if ((activity.basic?.plan?.length ?: 0) > 80) 120.dp else 90.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (isCompleted) colors.coral else colors.coral.copy(alpha = 0.2f),
                                    colors.coral.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
            }
        }

        // ── Right: Activity card ─────────────────────────────────────────────
        ActivityItemCard(
            activity    = activity,
            isCompleted = isCompleted,
            isLocked    = isLocked,
            isNext      = isNext,
            alpha       = alpha,
            colors      = colors,
            onClick     = onClick
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACTIVITY ITEM CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActivityItemCard(
    activity: Activity,
    isCompleted: Boolean,
    isLocked: Boolean,
    isNext: Boolean,
    alpha: Float,
    colors: AppColorScheme,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isLocked, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> colors.coral.copy(alpha = 0.08f)
                isNext      -> colors.bgSurface
                else        -> colors.bgSurface
            }
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(if (isNext) 3.5.dp else 1.5.dp),
        border = when {
            isCompleted -> BorderStroke(1.5.dp, colors.coral.copy(alpha = 0.5f))
            isNext      -> BorderStroke(2.dp, colors.coral.copy(alpha = 0.4f))
            else        -> null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = activity.title ?: "Activity ${activity.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    lineHeight = 18.sp
                )

                // Status chip
                when {
                    isCompleted -> StatusChip("✓ Done", colors.coral, colors)
                    isNext      -> StatusChip("▶ Next", colors.coral.copy(alpha = 0.8f), colors)
                    isLocked    -> StatusChip("🔒", colors.textPrimary.copy(alpha = 0.35f), colors)
                }
            }

            // Plan preview
            activity.basic?.plan?.let { plan ->
                if (plan.isNotBlank()) {
                    Text(
                        text = plan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary.copy(alpha = 0.65f),
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }
            }

            // Meta row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                activity.meta?.timeMinutes?.let {
                    MiniChip("⏱️ ${it}m", colors)
                }
                activity.meta?.materials?.size?.let { count ->
                    if (count > 0) MiniChip("🧩 $count items", colors)
                }
                Spacer(Modifier.weight(1f))
                // Level
                Text(
                    "L${activity.level}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.coral.copy(alpha = 0.7f)
                )
            }

            // Action button
            Button(
                onClick = onClick,
                enabled = !isLocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isCompleted -> colors.textPrimary.copy(alpha = 0.08f)
                        else        -> colors.coral
                    },
                    disabledContainerColor = colors.textPrimary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (!isLocked && !isCompleted) 3.dp else 0.dp,
                    pressedElevation = if (!isLocked && !isCompleted) 6.dp else 0.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when {
                            isCompleted -> "✓ Completed"
                            isLocked    -> "🔒 Complete previous"
                            else        -> "Start Activity"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isCompleted -> colors.textPrimary.copy(alpha = 0.4f)
                            isLocked    -> colors.textPrimary.copy(alpha = 0.35f)
                            else        -> Color.White
                        }
                    )
                    if (!isCompleted && !isLocked) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color, colors: AppColorScheme) {
    Surface(
        color = color.copy(alpha = 0.14f),
        modifier = Modifier
            .padding(start = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(0.8.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MiniChip(text: String, colors: AppColorScheme) {
    Surface(
        color = colors.bgMain,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(0.8.dp, colors.textPrimary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}