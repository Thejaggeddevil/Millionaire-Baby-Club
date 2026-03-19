package com.example.babyparenting.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.AgeGroup
import com.example.babyparenting.data.model.JourneyProgress
import com.example.babyparenting.data.model.Milestone
import com.example.babyparenting.ui.components.FootstepsLayer
import com.example.babyparenting.ui.components.MilestoneCard
import com.example.babyparenting.ui.components.PathCanvas
import com.example.babyparenting.ui.components.SectionHeader
import com.example.babyparenting.ui.components.computeNodePositions
import com.example.babyparenting.ui.theme.AppColors
import com.example.babyparenting.viewmodel.JourneyViewModel
import kotlin.math.roundToInt

private val SEGMENT_DP: Dp = 130.dp

@Composable
fun BabyJourneyScreen(
    viewModel: JourneyViewModel,
    onMilestoneTapped: (Milestone) -> Unit,
    onSettingsTapped: () -> Unit,
    onParentHubTapped: () -> Unit
) {
    val milestones     by viewModel.visibleMilestones.collectAsState()
    val ageGroups      by viewModel.ageGroups.collectAsState()
    val progress       by viewModel.progress.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val loadError      by viewModel.loadError.collectAsState()
    val completedCount  = milestones.count { it.isCompleted }
    var showAgeDialog   by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppColors.BgMain)
            .statusBarsPadding()
    ) {
        JourneyHeader(
            progress          = progress,
            onEditAge         = { showAgeDialog = true },
            onSettingsTapped  = onSettingsTapped,
            onParentHubTapped = onParentHubTapped
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                isLoading         -> LoadingView()
                loadError != null -> ErrorView(loadError!!) { viewModel.reloadDatasets() }
                milestones.isEmpty() -> EmptyView()
                else -> JourneyMap(
                    milestones         = milestones,
                    ageGroups          = ageGroups,
                    completedCount     = completedCount,
                    onMilestoneTapped  = { m -> if (!viewModel.isLocked(m)) onMilestoneTapped(m) },
                    onToggleCompletion = { id -> viewModel.markComplete(id) },
                    isLocked           = { viewModel.isLocked(it) }
                )
            }
        }
    }

    if (showAgeDialog) {
        AgeDialog(
            currentAge = progress.childAgeMonths,
            childName  = progress.childName,
            onConfirm  = { name, months ->
                viewModel.setChildName(name)
                viewModel.setChildAge(months)
                showAgeDialog = false
            },
            onDismiss = { showAgeDialog = false }
        )
    }
}

// ── Journey Map ───────────────────────────────────────────────────────────────

@Composable
private fun JourneyMap(
    milestones: List<Milestone>,
    ageGroups: List<AgeGroup>,
    completedCount: Int,
    onMilestoneTapped: (Milestone) -> Unit,
    onToggleCompletion: (String) -> Unit,
    isLocked: (Milestone) -> Boolean
) {
    val totalH          = SEGMENT_DP * (milestones.size + 2)
    val density         = LocalDensity.current
    val screenWidthDp   = LocalConfiguration.current.screenWidthDp.dp
    val wPx             = with(density) { screenWidthDp.toPx() }
    val hPx             = with(density) { totalH.toPx() }
    val nodes           = computeNodePositions(milestones.size, wPx, hPx)

    Box(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(totalH)) {
            PathCanvas(nodes, completedCount, Modifier.fillMaxWidth().height(totalH))
            FootstepsLayer(nodes, completedCount, Modifier.fillMaxWidth().height(totalH))

            milestones.forEachIndexed { gIdx, milestone ->
                if (gIdx >= nodes.size) return@forEachIndexed

                val isFirstInGroup = gIdx == 0 || milestones[gIdx - 1].ageGroupId != milestone.ageGroupId
                if (isFirstInGroup) {
                    val group   = ageGroups.find { it.id == milestone.ageGroupId }
                    if (group != null) {
                        val headerY = with(density) { nodes[gIdx].y.toDp() } - 50.dp
                        SectionHeader(
                            group    = group,
                            modifier = Modifier
                                .offset { IntOffset(with(density) { 12.dp.roundToPx() }, headerY.roundToPx()) }
                                .widthIn(max = screenWidthDp - 24.dp)
                        )
                    }
                }

                val node      = nodes[gIdx]
                val isLeft    = gIdx % 2 == 0
                val nodeXDp   = with(density) { node.x.toDp() }
                val nodeYDp   = with(density) { node.y.toDp() }
                val cardWidth = 160.dp
                val cardX     = if (isLeft) nodeXDp + 18.dp else nodeXDp - cardWidth - 18.dp
                val locked    = isLocked(milestone)

                Box(
                    Modifier.offset { IntOffset(cardX.roundToPx(), (nodeYDp - 22.dp).roundToPx()) }
                        .widthIn(max = cardWidth)
                ) {
                    MilestoneCard(
                        milestone          = milestone,
                        index              = gIdx,
                        isLocked           = locked,
                        onClick            = { if (!locked) onMilestoneTapped(milestone) },
                        onToggleCompletion = { if (!locked && !milestone.isCompleted) onToggleCompletion(milestone.id) }
                    )
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun JourneyHeader(
    progress: JourneyProgress,
    onEditAge: () -> Unit,
    onSettingsTapped: () -> Unit,
    onParentHubTapped: () -> Unit
) {
    val animProg by animateFloatAsState(progress.progressFraction, spring(Spring.DampingRatioNoBouncy), label = "p")

    Column(
        Modifier.fillMaxWidth().background(AppColors.BgHeader)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (progress.childName.isNotBlank()) "${progress.childName}'s Journey" else "Baby Growth Journey",
                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.TextPrimary
                )
                Text("Track every milestone with love", fontSize = 12.sp, color = AppColors.TextSecondary)
            }

            // Parent Hub button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Lavender.copy(alpha = 0.18f))
                    .clickable { onParentHubTapped() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Parent Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Lavender)
            }

            Spacer(Modifier.width(8.dp))

            // Profile / settings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(38.dp).clip(CircleShape)
                    .background(AppColors.Coral.copy(alpha = 0.15f)).clickable { onSettingsTapped() }
            ) {
                Icon(Icons.Default.Person, null, tint = AppColors.Coral, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(AppColors.Coral.copy(alpha = 0.12f))
                    .clickable { onEditAge() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    if (progress.childAgeMonths == 0) "Set Age ▾" else formatAge(progress.childAgeMonths) + " ▾",
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Coral
                )
            }

            Spacer(Modifier.width(10.dp))

            LinearProgressIndicator(
                progress   = animProg,
                modifier   = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)),
                color      = AppColors.Coral,
                trackColor = AppColors.Border
            )

            Spacer(Modifier.width(10.dp))

            Text(
                "${progress.completedMilestones}/${progress.totalMilestones}",
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Coral
            )
        }
    }
}

// ── State views ───────────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxWidth().padding(top = 120.dp), Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.Coral, strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(16.dp))
            Text("Loading milestones…", fontSize = 14.sp, color = AppColors.TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text("Only happens once", fontSize = 12.sp, color = AppColors.TextMuted)
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Could not load data", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 12.sp, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Coral), shape = RoundedCornerShape(10.dp)) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(15.dp), tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
private fun EmptyView() {
    Box(Modifier.fillMaxWidth().padding(top = 80.dp), Alignment.TopCenter) {
        Text("Complete the current batch to unlock more",
            fontSize = 14.sp, color = AppColors.TextSecondary, textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp))
    }
}

// ── Age dialog ────────────────────────────────────────────────────────────────

@Composable
private fun AgeDialog(currentAge: Int, childName: String, onConfirm: (String, Int) -> Unit, onDismiss: () -> Unit) {
    var sliderVal by remember { mutableStateOf(currentAge.toFloat()) }
    var nameInput by remember { mutableStateOf(childName) }
    val months = sliderVal.roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = AppColors.BgElevated,
        shape            = RoundedCornerShape(18.dp),
        title = { Text("Child's Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameInput, onValueChange = { nameInput = it },
                    label = { Text("Child's name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Coral, unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Coral, focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary,
                        focusedContainerColor = AppColors.BgSurface, unfocusedContainerColor = AppColors.BgSurface
                    )
                )
                Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Current age", fontSize = 13.sp, color = AppColors.TextSecondary)
                        Text(formatAge(months), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Coral)
                    }
                    Slider(
                        value = sliderVal, onValueChange = { sliderVal = it },
                        valueRange = 0f..144f, steps = 143,
                        colors = SliderDefaults.colors(thumbColor = AppColors.Coral, activeTrackColor = AppColors.Coral, inactiveTrackColor = AppColors.Border)
                    )
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Newborn", fontSize = 10.sp, color = AppColors.TextMuted)
                        Text("12 years", fontSize = 10.sp, color = AppColors.TextMuted)
                    }
                    if (months > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text("Past milestones will be auto-completed", fontSize = 11.sp, color = AppColors.Coral)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nameInput.trim(), months) },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Coral),
                shape = RoundedCornerShape(10.dp)) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AppColors.TextMuted) }
        }
    )
}

private fun formatAge(months: Int): String = when {
    months == 0      -> "Newborn"
    months < 12      -> "$months month${if (months == 1) "" else "s"}"
    months == 12     -> "1 year"
    months % 12 == 0 -> "${months / 12} years"
    else             -> "${months / 12} yr ${months % 12} mo"
}