package com.example.babyparenting.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.babyparenting.data.model.DatasetSource
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
    onAdminTapped: () -> Unit
) {
    val milestones    by viewModel.filteredMilestones.collectAsState()
    val ageGroups     by viewModel.ageGroups.collectAsState()
    val progress      by viewModel.progress.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()
    val loadError     by viewModel.loadError.collectAsState()
    val activeFilter  by viewModel.activeFilter.collectAsState()
    val completedCount = milestones.count { it.isCompleted }
    var showAgeDialog  by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0EB))
            .statusBarsPadding()
    ) {
        JourneyHeader(
            progress     = progress,
            onEditAge    = { showAgeDialog = true },
            onAdminTapped = onAdminTapped
        )
        FilterRow(
            activeFilter = activeFilter,
            onFilter     = { viewModel.toggleFilter(it) },
            onClear      = { viewModel.clearFilter() }
        )

        when {
            isLoading        -> LoadingView()
            loadError != null -> ErrorView(loadError!!) { viewModel.reloadDatasets() }
            milestones.isEmpty() -> EmptyView()
            else -> JourneyMap(
                milestones         = milestones,
                ageGroups          = ageGroups,
                completedCount     = completedCount,
                onMilestoneTapped  = onMilestoneTapped,
                onToggleCompletion = { viewModel.toggleCompletion(it) }
            )
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
    onToggleCompletion: (String) -> Unit
) {
    val totalH = SEGMENT_DP * (milestones.size + ageGroups.size + 2)

    BoxWithConstraints(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
        val density = LocalDensity.current
        val wPx     = with(density) { maxWidth.toPx() }
        val hPx     = with(density) { totalH.toPx() }
        val nodes   = computeNodePositions(milestones.size, wPx, hPx)

        PathCanvas(nodes, completedCount, Modifier.fillMaxWidth().height(totalH))
        FootstepsLayer(nodes, completedCount, Modifier.fillMaxWidth().height(totalH))

        var nodeIdx = 0
        ageGroups.forEach { group ->
            val groupMs = milestones.filter { it.ageGroupId == group.id }
            if (groupMs.isEmpty()) return@forEach

            if (nodeIdx < nodes.size) {
                val headerY = with(density) { nodes[nodeIdx].y.toDp() } - 50.dp
                SectionHeader(
                    group = group,
                    modifier = Modifier
                        .offset { IntOffset(with(density) { 12.dp.roundToPx() }, headerY.roundToPx()) }
                        .widthIn(max = maxWidth - 24.dp)
                )
            }

            groupMs.forEach { milestone ->
                val gIdx = milestones.indexOf(milestone)
                if (gIdx < 0 || gIdx >= nodes.size) return@forEach
                val node      = nodes[gIdx]
                val isLeft    = gIdx % 2 == 0
                val nodeXDp   = with(density) { node.x.toDp() }
                val nodeYDp   = with(density) { node.y.toDp() }
                val cardWidth = 160.dp
                val cardX     = if (isLeft) nodeXDp + 18.dp else nodeXDp - cardWidth - 18.dp

                Box(
                    Modifier
                        .offset { IntOffset(cardX.roundToPx(), (nodeYDp - 22.dp).roundToPx()) }
                        .widthIn(max = cardWidth)
                ) {
                    MilestoneCard(
                        milestone          = milestone,
                        index              = gIdx,
                        onClick            = { onMilestoneTapped(milestone) },
                        onToggleCompletion = { onToggleCompletion(milestone.id) }
                    )
                }
                nodeIdx++
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun JourneyHeader(
    progress: JourneyProgress,
    onEditAge: () -> Unit,
    onAdminTapped: () -> Unit
) {
    val animProg by animateFloatAsState(
        progress.progressFraction, spring(Spring.DampingRatioNoBouncy), label = "p"
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFE0CC), Color(0xFFF5F0EB))))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (progress.childName.isNotBlank()) "${progress.childName}'s Journey"
                    else "Baby Growth Journey",
                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D1B0E)
                )
                Text("Track every milestone with love", fontSize = 11.sp, color = Color(0xFFAA8877))
            }

            // Age chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFD6C2))
                    .clickable { onEditAge() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Default.ChildCare, null, tint = Color(0xFFD2691E), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (progress.childAgeMonths == 0) "Set age"
                    else formatAge(progress.childAgeMonths),
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD2691E)
                )
                Spacer(Modifier.width(3.dp))
                Icon(Icons.Default.Edit, null, tint = Color(0xFFD2691E), modifier = Modifier.size(10.dp))
            }

            Spacer(Modifier.width(4.dp))

            // Admin button
            IconButton(
                onClick  = onAdminTapped,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1565C0).copy(alpha = 0.10f))
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Panel",
                    tint     = Color(0xFF1565C0),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress   = animProg,
                modifier   = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)),
                color      = Color(0xFF1565C0),
                trackColor = Color(0xFFBBDEFB)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "${progress.completedMilestones}/${progress.totalMilestones}",
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)
            )
        }

        Spacer(Modifier.height(3.dp))
        Text(
            when {
                progress.progressFraction == 0f   -> "✨ Tap a milestone to begin"
                progress.progressFraction < 0.25f -> "🌱 Great start — keep going!"
                progress.progressFraction < 0.50f -> "🚀 Halfway — you're amazing!"
                progress.progressFraction < 0.75f -> "⭐ Almost there!"
                progress.progressFraction < 1f    -> "🎉 Nearly complete!"
                else                              -> "🏆 Journey complete!"
            },
            fontSize = 11.sp, color = Color(0xFF8B6B55)
        )
    }
}

// ── Filter chips ──────────────────────────────────────────────────────────────

@Composable
private fun FilterRow(
    activeFilter: DatasetSource?,
    onFilter: (DatasetSource) -> Unit,
    onClear: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = activeFilter == null, onClick = onClear,
            label    = { Text("All", fontSize = 11.sp) },
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF1565C0), selectedLabelColor = Color.White)
        )
        DatasetSource.values().forEach { source ->
            FilterChip(
                selected = activeFilter == source, onClick = { onFilter(source) },
                label    = { Text("${source.emoji} ${source.displayName}", fontSize = 11.sp) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(source.colorHex).copy(alpha = 0.85f),
                    selectedLabelColor     = Color.White)
            )
        }
    }
}

// ── State views ───────────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFFFF8B94), strokeWidth = 3.dp, modifier = Modifier.size(52.dp))
            Spacer(Modifier.height(16.dp))
            Text("Loading all milestones from datasets…", fontSize = 14.sp, color = Color(0xFFAA8877))
            Spacer(Modifier.height(4.dp))
            Text("This may take a moment on first launch", fontSize = 11.sp, color = Color(0xFF9E9E9E))
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("😕", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Couldn't load data", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D1B0E))
            Spacer(Modifier.height(8.dp))
            Text(message, fontSize = 12.sp, color = Color(0xFFAA8877), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Make sure CSV files are in:\napp/src/main/assets/datasets/",
                fontSize = 11.sp, color = Color(0xFF9E9E9E), textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Retry", color = Color.White)
            }
        }
    }
}

@Composable
private fun EmptyView() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔍", fontSize = 40.sp)
            Spacer(Modifier.height(10.dp))
            Text("No milestones match this filter", fontSize = 14.sp, color = Color(0xFF8B6B55), textAlign = TextAlign.Center)
        }
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
        shape            = RoundedCornerShape(20.dp),
        title = { Text("Your child's profile", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameInput, onValueChange = { nameInput = it },
                    label = { Text("Child's name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(formatAge(months), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1565C0), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text("Past milestones auto-complete", fontSize = 12.sp, color = Color(0xFF888888),
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = sliderVal, onValueChange = { sliderVal = it },
                    valueRange = 0f..144f, steps = 143,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF1565C0), activeTrackColor = Color(0xFF1565C0), inactiveTrackColor = Color(0xFFBBDEFB))
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Newborn", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                    Text("12 years", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nameInput.trim(), months) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatAge(months: Int): String = when {
    months == 0      -> "Newborn"
    months < 12      -> "$months month${if (months == 1) "" else "s"}"
    months == 12     -> "1 year"
    months % 12 == 0 -> "${months / 12} years"
    else             -> "${months / 12} yr ${months % 12} mo"
}