package com.example.babyparenting.ui.screens.millionaire

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.ProgressSummary
import com.example.babyparenting.data.model.DailyActivityResponse
import com.example.babyparenting.data.model.Strategy
import com.example.babyparenting.ui.viewmodel.MillionaireViewModel
import com.example.babyparenting.ui.theme.LocalAppColors
import com.example.babyparenting.ui.viewmodel.DailyActivityUiState
import com.example.babyparenting.ui.viewmodel.ProgressUiState
import com.example.babyparenting.ui.viewmodel.StrategiesUiState

@Composable
fun MillionaireClubScreen(
    viewModel: MillionaireViewModel,
    onStrategyClick: (strategyId: Int) -> Unit,
    onActivityClick: (activityId: Int, strategyId: Int) -> Unit
) {
    val strategiesState by viewModel.strategiesState.collectAsState()
    val dailyActivityState by viewModel.dailyActivityState.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    val colors = LocalAppColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Millionaire Baby Club",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Build thinking skills through daily activities",
                    fontSize = 14.sp,
                    color = colors.textPrimary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Daily Activity Section (Changes at midnight)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Today's Challenge",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (val state = dailyActivityState) {
                    is DailyActivityUiState.Success -> {
                        TodaysActivityCard(
                            activity = state.activity,
                            colors = colors,
                            onActivityClick = {
                                val activityId = state.activity.activity?.id?.toInt() ?: 0
                                val strategyId = state.activity.activity?.strategy_id ?: 0
                                if (activityId > 0) {
                                    onActivityClick(activityId, strategyId)
                                }
                            }
                        )
                    }
                    is DailyActivityUiState.Loading -> LoadingCard()
                    is DailyActivityUiState.Error -> ErrorCard(message = state.message)
                }
            }
        }

        // Progress Summary Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Your Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (val state = progressState) {
                    is ProgressUiState.Success -> {
                        ProgressSummaryCard(
                            progress = state.progress,
                            colors = colors
                        )
                    }
                    is ProgressUiState.Loading -> LoadingCard()
                    is ProgressUiState.Error -> {}
                    else -> {}
                }
            }
        }

        // Strategies Section with Horizontal Scrolling
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Learning Strategies",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                when (val state = strategiesState) {
                    is StrategiesUiState.Success -> {
                        if (state.strategies.isNotEmpty()) {
                            StrategiesHorizontalList(
                                strategies = state.strategies,
                                colors = colors,
                                onStrategyClick = onStrategyClick
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No strategies available",
                                    color = colors.textPrimary.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    is StrategiesUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.coral)
                        }
                    }
                    is StrategiesUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorCard(message = state.message)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * Daily Activity Card - Shows today's challenge with compelling design
 * Updates at midnight automatically
 */
@Composable
private fun TodaysActivityCard(
    activity: DailyActivityResponse,
    colors: com.example.babyparenting.ui.theme.AppColorScheme,
    onActivityClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onActivityClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.coral.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = colors.coral.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top section with label and arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Today's Challenge",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.coral
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activity.activity?.title ?: "No activity today",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        maxLines = 2
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open activity",
                    tint = colors.coral,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Top)
                )
            }

            // Activity metadata
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.coral.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱ ${activity.activity?.duration ?: 10} min",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "•",
                    fontSize = 13.sp,
                    color = colors.textPrimary.copy(alpha = 0.3f)
                )
                Text(
                    text = "Strategy ${activity.activity?.strategy_id ?: "N/A"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            // Call to action button
            Button(
                onClick = onActivityClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.coral
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start activity",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Challenge",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Progress Summary Card - Shows overall progress and achievements
 */
@Composable
private fun ProgressSummaryCard(
    progress: ProgressSummary,
    colors: com.example.babyparenting.ui.theme.AppColorScheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.bgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main progress info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Activities Completed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "${progress.completed_activities}/${progress.total_activities}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.coral
                )
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = (progress.completion_percentage / 100f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = colors.coral,
                    trackColor = colors.coral.copy(alpha = 0.2f)
                )
                Text(
                    text = "${progress.completion_percentage.toInt()}% Complete",
                    fontSize = 12.sp,
                    color = colors.textPrimary.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProgressStatItem(
                    label = "Completed",
                    value = progress.completed_activities.toString(),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                ProgressStatItem(
                    label = "Level",
                    value = "${progress.current_level}",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                ProgressStatItem(
                    label = "Streak",
                    value = "🔥",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual progress statistic item
 */
@Composable
private fun ProgressStatItem(
    label: String,
    value: String,
    colors: com.example.babyparenting.ui.theme.AppColorScheme,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(colors.coral.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.coral
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = colors.textPrimary.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Horizontally scrollable list of learning strategies
 * Each card has a button to view all activities for that strategy
 */
@Composable
private fun StrategiesHorizontalList(
    strategies: List<Strategy>,
    colors: com.example.babyparenting.ui.theme.AppColorScheme,
    onStrategyClick: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            count = strategies.size,
            key = { index -> strategies.getOrNull(index)?.id ?: index }
        ) { index ->
            strategies.getOrNull(index)?.let { strategy ->
                StrategyCard(
                    strategy = strategy,
                    colors = colors,
                    onClick = { onStrategyClick(strategy.id) }
                )
            }
        }
    }
}

/**
 * Individual Strategy Card
 * Shows strategy details and a button to view all activities
 */
@Composable
private fun StrategyCard(
    strategy: Strategy,
    colors: com.example.babyparenting.ui.theme.AppColorScheme,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.bgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Strategy info
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strategy.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    maxLines = 2
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👶",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${strategy.age_min}–${strategy.age_max} yrs",
                        fontSize = 11.sp,
                        color = colors.textPrimary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${strategy.completed_count}/${strategy.total_activities}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.coral
                )
                LinearProgressIndicator(
                    progress = if (strategy.total_activities > 0)
                        (strategy.completed_count.toFloat() / strategy.total_activities).coerceIn(0f, 1f)
                    else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = colors.coral,
                    trackColor = colors.coral.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action button - Click to view all activities
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.coral.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View activities",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Loading state card
 */
@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LocalAppColors.current.bgSurface),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = LocalAppColors.current.coral)
    }
}

/**
 * Error state card
 */
@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LocalAppColors.current.red.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Error",
                tint = LocalAppColors.current.red,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                fontSize = 12.sp,
                color = LocalAppColors.current.red,
                modifier = Modifier.weight(1f)
            )
        }
    }
}