package com.example.babyparenting.ui.screens.millionaire

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.local.UserManager
import com.example.babyparenting.ui.theme.AppColorScheme
import com.example.babyparenting.ui.theme.LocalAppColors
import com.example.babyparenting.ui.viewmodel.ActivityDetailUiState
import com.example.babyparenting.ui.viewmodel.CompletionUiState
import com.example.babyparenting.ui.viewmodel.MillionaireViewModel
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// ACTIVITY DETAIL SCREEN
// Tabs: Plan | Do | Review | Parent Guide | Tips
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: Int,
    strategyId: Int,
    viewModel: MillionaireViewModel,
    onBackClick: () -> Unit
) {
    val activityDetailState by viewModel.activityDetailState.collectAsState()
    val completionState     by viewModel.completionState.collectAsState()
    val completedActivities by viewModel.completedActivities.collectAsState()
    val colors = LocalAppColors.current

    val isCompleted = completedActivities.contains(activityId)

    LaunchedEffect(activityId) {
        viewModel.loadActivityDetail(activityId)
    }
    // ✅ Back navigation observer
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        if (navigationEvent) {
            viewModel.resetNavigationEvent()
            onBackClick()
        }
    }

    // ── Timer state ──────────────────────────────────────────────────────────
    var timerRunning  by remember { mutableStateOf(false) }
    var timerSeconds  by remember { mutableStateOf(0) }
    var timerTotal    by remember { mutableStateOf(300) } // default 5 min

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (timerRunning && timerSeconds > 0) {
                delay(1000L)
                timerSeconds--
            }
            if (timerSeconds == 0) timerRunning = false
        }
    }

    // ── Selected tab ─────────────────────────────────────────────────────────
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📋 Plan", "▶ Do", "🔁 Review", "👨‍👩‍👧 Guide", "💡 Tips")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
    ) {

        // ── Top Bar ──────────────────────────────────────────────────────────
        TopAppBar(
            title = {
                when (val s = activityDetailState) {
                    is ActivityDetailUiState.Success -> Text(
                        text = s.activity.title ?: "Activity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    else -> Text("Activity", fontSize = 16.sp, color = colors.textPrimary)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
            },
            actions = {
                if (isCompleted) {
                    Surface(
                        color  = Color(0xFF4CAF50).copy(alpha = 0.12f),
                        shape  = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.25f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                            Text(
                                text = "Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.bgSurface
            )
        )

        when (val state = activityDetailState) {

            is ActivityDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color       = colors.coral,
                            modifier    = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            "Loading activity...",
                            fontSize = 13.sp,
                            color    = colors.textPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            is ActivityDetailUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors    = CardDefaults.cardColors(containerColor = colors.bgSurface),
                        shape     = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("⚠️", fontSize = 36.sp)
                            Text(
                                state.message,
                                fontSize    = 13.sp,
                                color       = colors.red,
                                textAlign   = TextAlign.Center,
                                lineHeight  = 20.sp
                            )
                            Button(
                                onClick = { viewModel.loadActivityDetail(activityId) },
                                colors  = ButtonDefaults.buttonColors(containerColor = colors.coral),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            is ActivityDetailUiState.Success -> {
                val activity = state.activity

                // Set timer total from activity duration
                LaunchedEffect(activity.meta?.timeMinutes) {
                    val mins = activity.meta?.timeMinutes ?: 5
                    timerTotal = mins * 60
                    timerSeconds = mins * 60
                }

                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Hero Card ────────────────────────────────────────────
                    ActivityHeroCard(
                        title       = activity.title ?: "",
                        materials   = activity.meta?.materials ?: emptyList(),
                        timeMinutes = activity.meta?.timeMinutes ?: 5,
                        timerSeconds = timerSeconds,
                        timerTotal   = timerTotal,
                        timerRunning = timerRunning,
                        isCompleted  = isCompleted,
                        colors       = colors,
                        onTimerToggle = {
                            timerRunning = !timerRunning
                            if (!timerRunning && timerSeconds == 0) {
                                timerSeconds = timerTotal
                            }
                        },
                        onTimerReset = {
                            timerRunning = false
                            timerSeconds = timerTotal
                        }
                    )

                    // ── Tab Row ──────────────────────────────────────────────
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor   = colors.bgSurface,
                        contentColor     = colors.coral,
                        edgePadding      = 0.dp,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color    = colors.coral,
                                    height   = 3.dp
                                )
                            }
                        },
                        divider = {
                            HorizontalDivider(
                                color = colors.textPrimary.copy(alpha = 0.08f),
                                thickness = 1.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected  = selectedTab == index,
                                onClick   = { selectedTab = index },
                                modifier  = Modifier.height(46.dp),
                                text = {
                                    Text(
                                        text  = tab,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) colors.coral else colors.textPrimary.copy(alpha = 0.5f)
                                    )
                                }
                            )
                        }
                    }

                    // ── Tab Content ──────────────────────────────────────────
                    AnimatedContent(
                        targetState = selectedTab,
                        modifier = Modifier.weight(1f),
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        }
                    ) { tab ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (tab) {

                                // ── PLAN TAB ─────────────────────────────────
                                0 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StepCard(
                                        emoji  = "🎯",
                                        title  = "What to Plan",
                                        text   = activity.basic?.plan ?: "No plan available",
                                        color  = colors.coral,
                                        colors = colors
                                    )
                                    activity.parentGuidance?.setup?.let {
                                        InfoCard(
                                            title  = "Setup Guidance",
                                            text   = it,
                                            emoji  = "🛠️",
                                            colors = colors
                                        )
                                    }
                                    activity.meta?.materials?.let { mats ->
                                        if (mats.isNotEmpty()) {
                                            MaterialsCard(materials = mats, colors = colors)
                                        }
                                    }
                                }

                                // ── DO TAB ───────────────────────────────────
                                1 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StepCard(
                                        emoji  = "▶️",
                                        title  = "What to Do",
                                        text   = activity.basic?.do_ ?: "No instructions available",
                                        color  = Color(0xFF4CAF50),
                                        colors = colors
                                    )
                                    activity.parentGuidance?.do_?.let {
                                        InfoCard(
                                            title  = "How to Guide Your Child",
                                            text   = it,
                                            emoji  = "👐",
                                            colors = colors
                                        )
                                    }
                                    activity.parentGuidance?.planQuestions?.let { qs ->
                                        if (qs.isNotEmpty()) {
                                            QuestionsCard(
                                                title     = "Questions to Ask",
                                                questions = qs,
                                                colors    = colors
                                            )
                                        }
                                    }
                                }

                                // ── REVIEW TAB ───────────────────────────────
                                2 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StepCard(
                                        emoji  = "🔁",
                                        title  = "Review",
                                        text   = activity.basic?.review ?: "No review available",
                                        color  = Color(0xFF2196F3),
                                        colors = colors
                                    )
                                    activity.parentGuidance?.reviewPrompts?.let { rp ->
                                        if (rp.isNotEmpty()) {
                                            QuestionsCard(
                                                title     = "Review Prompts",
                                                questions = rp,
                                                colors    = colors
                                            )
                                        }
                                    }
                                    activity.parentGuidance?.repeat?.let {
                                        InfoCard(
                                            title  = "Repeat Guidance",
                                            text   = it,
                                            emoji  = "🔄",
                                            colors = colors
                                        )
                                    }
                                }

                                // ── PARENT GUIDE TAB ─────────────────────────
                                3 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    activity.help?.dialogue?.let {
                                        DialogueCard(dialogue = it, colors = colors)
                                    }
                                    activity.help?.indicators?.let { ind ->
                                        if (ind.isNotEmpty()) {
                                            ChecklistCard(
                                                title = "✅ Success Indicators",
                                                items = ind,
                                                itemColor = Color(0xFF4CAF50),
                                                colors = colors
                                            )
                                        }
                                    }
                                    activity.parentGuidance?.planQuestions?.let { qs ->
                                        if (qs.isNotEmpty()) {
                                            QuestionsCard(
                                                title     = "Questions During Activity",
                                                questions = qs,
                                                colors    = colors
                                            )
                                        }
                                    }
                                }

                                // ── TIPS TAB ─────────────────────────────────
                                4 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    activity.help?.mistakes?.let { mistakes ->
                                        if (mistakes.isNotEmpty()) {
                                            ChecklistCard(
                                                title     = "⚠️ Common Mistakes",
                                                items     = mistakes,
                                                itemColor = Color(0xFFFF9800),
                                                colors    = colors
                                            )
                                        }
                                    }
                                    activity.help?.examples?.let { examples ->
                                        if (examples.isNotEmpty()) {
                                            ChecklistCard(
                                                title     = "💡 Tips & Examples",
                                                items     = examples,
                                                itemColor = colors.coral,
                                                colors    = colors
                                            )
                                        }
                                    }
                                }
                            } // end when(tab)
                            Spacer(Modifier.height(80.dp))
                        } // end Column (scrollable)
                    } // end AnimatedContent

                    // ── Bottom Complete Button ────────────────────────────────
                    BottomCompleteBar(
                        isCompleted      = isCompleted,
                        completionState  = completionState,
                        colors           = colors,
                        onComplete = {
                            if (!isCompleted) {
                                val userId = UserManager.getUserId(viewModel.getContext())
                                viewModel.markActivityCompleted(userId, activityId, strategyId)
                            }
                        }
                    )
                }
            }

            else -> {}
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO CARD  —  title + timer + meta chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActivityHeroCard(
    title: String,
    materials: List<String>,
    timeMinutes: Int,
    timerSeconds: Int,
    timerTotal: Int,
    timerRunning: Boolean,
    isCompleted: Boolean,
    colors: AppColorScheme,
    onTimerToggle: () -> Unit,
    onTimerReset: () -> Unit
) {
    val progress = if (timerTotal > 0) timerSeconds.toFloat() / timerTotal else 0f
    val animProgress by animateFloatAsState(progress, tween(800))
    val progressColor = when {
        animProgress > 0.5f -> colors.coral
        animProgress > 0.25f -> Color(0xFFFF9800)
        else -> Color(0xFFE53935)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.bgSurface),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.coral.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isCompleted) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(13.dp))
                            Text("Done", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Meta chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaChip(icon = "⏱️", label = "${timeMinutes} min", colors = colors)
                if (materials.isNotEmpty()) {
                    MetaChip(icon = "🧸", label = "${materials.size} materials", colors = colors)
                }
            }

            // Timer section
            Surface(
                color  = colors.bgMain,
                shape  = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text  = "Timer",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary.copy(alpha = 0.45f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = formatTimerDisplay(timerSeconds),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (timerRunning) progressColor else colors.textPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onTimerReset,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.textPrimary.copy(alpha = 0.07f))
                            ) {
                                Icon(Icons.Default.Refresh, "Reset", tint = colors.textPrimary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onTimerToggle,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(colors.coral, colors.coral.copy(alpha = 0.8f))
                                        )
                                    )
                            ) {
                                Icon(
                                    if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    "Timer",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.coral.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animProgress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(progressColor.copy(alpha = 0.8f), progressColor)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP CARD  —  colored left border
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepCard(
    emoji: String,
    title: String,
    text: String,
    color: Color,
    colors: AppColorScheme
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = colors.bgSurface),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left accent
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(color, color.copy(alpha = 0.4f))),
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.03f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color  = color.copy(alpha = 0.12f),
                        shape  = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 16.sp)
                        }
                    }
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Text(
                    text = text,
                    fontSize = 13.sp,
                    color = colors.textPrimary.copy(alpha = 0.85f),
                    lineHeight = 21.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INFO CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InfoCard(
    title: String,
    text: String,
    emoji: String,
    colors: AppColorScheme
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = colors.bgSurface),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color  = colors.bgMain,
                    shape  = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 15.sp)
                    }
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.06f), thickness = 1.dp)
            Text(
                text = text,
                fontSize = 13.sp,
                color = colors.textPrimary.copy(alpha = 0.8f),
                lineHeight = 21.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MATERIALS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MaterialsCard(
    materials: List<String>,
    colors: AppColorScheme
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = colors.bgSurface),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color  = colors.coral.copy(alpha = 0.1f),
                    shape  = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🧸", fontSize = 15.sp)
                    }
                }
                Text(
                    "Materials Needed",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.06f), thickness = 1.dp)
            materials.forEachIndexed { i, material ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color  = colors.coral.copy(alpha = 0.12f),
                        shape  = CircleShape,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${i + 1}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.coral)
                        }
                    }
                    Text(
                        text = material,
                        fontSize = 12.sp,
                        color = colors.textPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUESTIONS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuestionsCard(
    title: String,
    questions: List<String>,
    colors: AppColorScheme
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = colors.coral.copy(alpha = 0.05f)),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, colors.coral.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text("❓", fontSize = 13.sp)
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.coral
                )
            }
            HorizontalDivider(color = colors.coral.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(Modifier.height(10.dp))
            questions.forEachIndexed { i, q ->
                if (i > 0) {
                    HorizontalDivider(
                        color = colors.textPrimary.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color    = colors.coral.copy(alpha = 0.18f),
                        shape    = CircleShape,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${i + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.coral
                            )
                        }
                    }
                    Text(
                        text = q,
                        fontSize = 12.sp,
                        color = colors.textPrimary.copy(alpha = 0.85f),
                        lineHeight = 19.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHECKLIST CARD  (mistakes / indicators / tips)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChecklistCard(
    title: String,
    items: List<String>,
    itemColor: Color,
    colors: AppColorScheme
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = colors.bgSurface),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(itemColor)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            HorizontalDivider(color = itemColor.copy(alpha = 0.15f), thickness = 1.dp)
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(itemColor)
                    )
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        color = colors.textPrimary.copy(alpha = 0.8f),
                        lineHeight = 19.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOGUE CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DialogueCard(dialogue: String, colors: AppColorScheme) {
    val green = Color(0xFF4CAF50)
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = green.copy(alpha = 0.06f)),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, green.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(color = green.copy(alpha = 0.12f), shape = CircleShape, modifier = Modifier.size(30.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("💬", fontSize = 14.sp) }
                }
                Text(
                    "Example Dialogue",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = green
                )
            }
            HorizontalDivider(color = green.copy(alpha = 0.15f), thickness = 1.dp)
            Text(
                text = "\"$dialogue\"",
                fontSize = 13.sp,
                color = colors.textPrimary.copy(alpha = 0.8f),
                lineHeight = 21.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM COMPLETE BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomCompleteBar(
    isCompleted: Boolean,
    completionState: CompletionUiState,
    colors: AppColorScheme,
    onComplete: () -> Unit
) {
    Surface(
        modifier      = Modifier.fillMaxWidth(),
        color         = colors.bgSurface,
        shadowElevation = 16.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                isCompleted -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        color    = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Activity Completed!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                completionState is CompletionUiState.Loading -> {
                    Button(
                        onClick  = {},
                        enabled  = false,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors   = ButtonDefaults.buttonColors(disabledContainerColor = colors.coral.copy(alpha = 0.5f)),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Completing...", color = Color.White, fontSize = 14.sp)
                    }
                }
                else -> {
                    Button(
                        onClick  = onComplete,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = colors.coral),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Mark as Complete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// META CHIP
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MetaChip(icon: String, label: String, colors: AppColorScheme) {
    Surface(
        color    = colors.coral.copy(alpha = 0.1f),
        shape    = RoundedCornerShape(20.dp),
        border   = BorderStroke(1.dp, colors.coral.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 11.sp)
            Text(label, fontSize = 11.sp, color = colors.coral, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun formatTimerDisplay(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}