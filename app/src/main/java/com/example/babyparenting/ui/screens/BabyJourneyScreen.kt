package com.example.babyparenting.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.babyparenting.viewmodel.JourneyViewModel
import kotlin.math.roundToInt

private val SEGMENT_DP: Dp = 130.dp

@Composable
fun BabyJourneyScreen(
    viewModel: JourneyViewModel,
    onMilestoneTapped: (Milestone) -> Unit,
    onSettingsTapped: () -> Unit
) {
    val milestones    by viewModel.visibleMilestones.collectAsState()
    val ageGroups     by viewModel.ageGroups.collectAsState()
    val progress      by viewModel.progress.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()
    val loadError     by viewModel.loadError.collectAsState()
    val completedCount = milestones.count { it.isCompleted }
    var showAgeDialog  by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0EB))
            .statusBarsPadding()
    ) {
        JourneyHeader(
            progress        = progress,
            onEditAge       = { showAgeDialog = true },
            onSettingsTapped = onSettingsTapped
        )

        // weight(1f) here gives the map all remaining vertical space after the header
        // This is the correct place — direct child of Column
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                isLoading         -> LoadingView()
                loadError != null -> ErrorView(loadError!!) { viewModel.reloadDatasets() }
                milestones.isEmpty() -> EmptyView()
                else -> JourneyMap(
                    milestones         = milestones,
                    ageGroups          = ageGroups,
                    completedCount     = completedCount,
                    onMilestoneTapped  = { milestone ->
                        if (!viewModel.isLocked(milestone)) onMilestoneTapped(milestone)
                    },
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
    // Explicit total height — the canvas must have a known size
    val totalH  = SEGMENT_DP * (milestones.size + 2)

    // Get screen width WITHOUT BoxWithConstraints (avoids the scroll+fill crash)
    val density     = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val wPx         = with(density) { screenWidthDp.toPx() }
    val hPx         = with(density) { totalH.toPx() }
    val nodes       = computeNodePositions(milestones.size, wPx, hPx)

    // verticalScroll on the OUTER box — the inner Box has explicit height, never fillMaxSize
    Box(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Inner Box with EXPLICIT height — no fillMaxSize, no fillMaxHeight inside scroll
        Box(
            Modifier
                .fillMaxWidth()
                .height(totalH)
        ) {
            PathCanvas(nodes, completedCount, Modifier.fillMaxWidth().height(totalH))
            FootstepsLayer(nodes, completedCount, Modifier.fillMaxWidth().height(totalH))

            milestones.forEachIndexed { gIdx, milestone ->
                if (gIdx >= nodes.size) return@forEachIndexed

                // Section header at start of each age group
                val isFirstInGroup = gIdx == 0 ||
                        milestones[gIdx - 1].ageGroupId != milestone.ageGroupId

                if (isFirstInGroup) {
                    val group = ageGroups.find { it.id == milestone.ageGroupId }
                    if (group != null) {
                        val headerY = with(density) { nodes[gIdx].y.toDp() } - 50.dp
                        SectionHeader(
                            group    = group,
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        with(density) { 12.dp.roundToPx() },
                                        headerY.roundToPx()
                                    )
                                }
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
                    Modifier
                        .offset { IntOffset(cardX.roundToPx(), (nodeYDp - 22.dp).roundToPx()) }
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
    onSettingsTapped: () -> Unit
) {
    val animProg by animateFloatAsState(
        progress.progressFraction,
        spring(Spring.DampingRatioNoBouncy),
        label = "progress"
    )

    // Warm gradient top — same original design
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFE0CC), Color(0xFFF5F0EB))
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            // Title block
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (progress.childName.isNotBlank())
                        "${progress.childName}'s Journey"
                    else "Baby Growth Journey",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color(0xFF2D1B0E)
                )
                Text(
                    "Track every milestone with love",
                    fontSize = 12.sp,
                    color    = Color(0xFFAA8877)
                )
            }

            // Settings / profile icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD6C2))
                    .clickable { onSettingsTapped() }
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Settings",
                    tint     = Color(0xFFD2691E),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Age + progress row
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Tap to set age
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFD6C2).copy(alpha = 0.6f))
                    .clickable { onEditAge() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (progress.childAgeMonths == 0) "Set Age ▾"
                    else formatAge(progress.childAgeMonths) + " ▾",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF8B4513)
                )
            }

            Spacer(Modifier.width(10.dp))

            LinearProgressIndicator(
                progress   = animProg,
                modifier   = Modifier
                    .weight(1f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = Color(0xFFFF8B94),
                trackColor = Color(0xFFFFD6C2)
            )

            Spacer(Modifier.width(10.dp))

            Text(
                "${progress.completedMilestones}/${progress.totalMilestones}",
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFFF8B94)
            )
        }
    }
}

// ── State views ───────────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color       = Color(0xFFFF8B94),
                strokeWidth = 3.dp,
                modifier    = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Loading milestones…", fontSize = 14.sp, color = Color(0xFFAA8877))
            Spacer(Modifier.height(4.dp))
            Text("Only happens once", fontSize = 12.sp, color = Color(0xFFBBBBBB))
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Could not load data", fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold, color = Color(0xFF2D1B0E))
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 12.sp, color = Color(0xFFAA8877), textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text("Add CSV files to: assets/datasets/",
            fontSize = 11.sp, color = Color(0xFFBBBBBB), textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8B94)),
            shape   = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(15.dp), tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
private fun EmptyView() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(top = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            "Complete the current batch to unlock more",
            fontSize  = 14.sp,
            color     = Color(0xFFAA8877),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// ── Age dialog ────────────────────────────────────────────────────────────────

@Composable
private fun AgeDialog(
    currentAge: Int,
    childName: String,
    onConfirm: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderVal by remember { mutableStateOf(currentAge.toFloat()) }
    var nameInput by remember { mutableStateOf(childName) }
    val months = sliderVal.roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(18.dp),
        title = { Text("Child's Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = nameInput,
                    onValueChange = { nameInput = it },
                    label         = { Text("Child's name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp)
                )
                Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Current age", fontSize = 13.sp, color = Color(0xFF777777))
                        Text(
                            formatAge(months),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFFFF8B94)
                        )
                    }
                    Slider(
                        value         = sliderVal,
                        onValueChange = { sliderVal = it },
                        valueRange    = 0f..144f,
                        steps         = 143,
                        colors        = SliderDefaults.colors(
                            thumbColor         = Color(0xFFFF8B94),
                            activeTrackColor   = Color(0xFFFF8B94),
                            inactiveTrackColor = Color(0xFFFFD6C2)
                        )
                    )
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Newborn", fontSize = 10.sp, color = Color(0xFFBBBBBB))
                        Text("12 years", fontSize = 10.sp, color = Color(0xFFBBBBBB))
                    }
                    if (months > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Past milestones will be auto-completed",
                            fontSize = 11.sp,
                            color    = Color(0xFFFF8B94)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameInput.trim(), months) },
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8B94)),
                shape   = RoundedCornerShape(10.dp)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF9E9E9E))
            }
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