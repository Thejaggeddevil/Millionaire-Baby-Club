package com.example.babyparenting.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.Milestone
import com.example.babyparenting.data.model.UiState
import com.example.babyparenting.network.model.AdviceResponse
import com.example.babyparenting.viewmodel.JourneyViewModel

@Composable
fun AdviceScreen(viewModel: JourneyViewModel, onBack: () -> Unit) {
    val adviceState       by viewModel.adviceState.collectAsState()
    val selectedMilestone by viewModel.selectedMilestone.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6F1))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,

            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEEE0)).padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF5C3D2E))
            }
            Column(Modifier.weight(1f)) {
                Text(selectedMilestone?.title ?: "Advice", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D1B0E), maxLines = 1)
                if (selectedMilestone != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${selectedMilestone!!.iconEmoji}  ${selectedMilestone!!.ageRange}", fontSize = 12.sp, color = Color(0xFFAA8877))
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp))
                                .background(Color(selectedMilestone!!.accentColor).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${selectedMilestone!!.source.emoji} ${selectedMilestone!!.source.displayName}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(selectedMilestone!!.accentColor))
                        }
                    }
                }
            }
        }

        // Content
        AnimatedContent(
            targetState = adviceState,
            transitionSpec = {
                (fadeIn(tween(320)) + slideInVertically(spring(Spring.DampingRatioMediumBouncy)) { it / 4 }) togetherWith fadeOut(tween(180))
            },
            label = "advice"
        ) { state ->
            when (state) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFFF8B94), strokeWidth = 3.5.dp, modifier = Modifier.size(52.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Getting personalised advice…", fontSize = 14.sp, color = Color(0xFFAA8877))
                    }
                }
                is UiState.Success -> AdviceContent(state.data, selectedMilestone) {
                    selectedMilestone?.let { viewModel.toggleCompletion(it.id) }
                }
                is UiState.Error   -> Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFE57373), modifier = Modifier.size(52.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Couldn't load advice", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D1B0E))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, fontSize = 13.sp, color = Color(0xFFAA8877), textAlign = TextAlign.Center)
                        if (state.retryable) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.retryAdvice() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is UiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Select a milestone to see advice 🌟", fontSize = 14.sp, color = Color(0xFFAA8877))
                }
            }
        }
    }
}

// ── Advice Content ────────────────────────────────────────────────────────────

@Composable
private fun AdviceContent(advice: AdviceResponse, milestone: Milestone?, onMarkDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero
        val accent = milestone?.let { Color(it.accentColor) } ?: Color(0xFFFF8B94)
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = accent.copy(0.3f), spotColor = accent.copy(0.3f))
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.7f))))
                .padding(20.dp)
        ) {
            Column {
                if (advice.domain.isNotBlank()) {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.22f)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                        Text(advice.domain.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Text(
                    advice.title.ifBlank { milestone?.title ?: "" },
                    fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 26.sp
                )
            }
        }

        // Advice sections
        if (advice.goal.isNotBlank())     InfoCard("🎯", "Goal",            advice.goal,       Color(0xFF5E9BE0))
        if (advice.why.isNotBlank())      InfoCard("💡", "Why it matters",  advice.why,        Color(0xFFF5A623))
        if (advice.how.isNotBlank())      InfoCard("🛠️", "How to do it",    advice.how,        Color(0xFF66BB6A))
        if (advice.steps.isNotEmpty())    StepsCard(advice.steps)

        if (advice.dos.isNotEmpty() || advice.donts.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (advice.dos.isNotEmpty())
                    ListCard("✅  Do", advice.dos, Color(0xFFE8F5E9), Color(0xFF81C784), Color(0xFF2E7D32), Modifier.weight(1f))
                if (advice.donts.isNotEmpty())
                    ListCard("❌  Don't", advice.donts, Color(0xFFFFEBEE), Color(0xFFEF9A9A), Color(0xFFC62828), Modifier.weight(1f))
            }
        }

        if (advice.tip.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.Top,

                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4))))
                    .border(1.dp, Color(0xFFFFD600).copy(0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text("💡", fontSize = 18.sp)
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("Quick Tip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B7000))
                    Spacer(Modifier.height(3.dp))
                    Text(advice.tip, fontSize = 13.sp, color = Color(0xFF5C4A00), lineHeight = 19.sp)
                }
            }
        }

        // Extra fields
        if (advice.example.isNotBlank())  InfoCard("💬", "Example",    advice.example,    Color(0xFF9C27B0))
        if (advice.answer.isNotBlank())   InfoCard("✅", "Answer",     advice.answer,     Color(0xFF4CAF50))
        if (advice.scenario.isNotBlank()) InfoCard("📖", "Scenario",   advice.scenario,   Color(0xFFFF9800))

        // Mark done button
        Button(
            onClick  = onMarkDone,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (milestone?.isCompleted == true) Color(0xFF66BB6A) else Color(0xFF1565C0)
            )
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                if (milestone?.isCompleted == true) "✓ Completed!" else "Mark as complete",
                color = Color.White, fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun InfoCard(icon: String, label: String, body: String, tint: Color) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(0.07f))
            .border(1.dp, tint.copy(0.18f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 15.sp)
            Spacer(Modifier.width(7.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tint)
        }
        Spacer(Modifier.height(7.dp))
        Text(body, fontSize = 13.sp, color = Color(0xFF3D2314), lineHeight = 20.sp)
    }
}

@Composable
private fun StepsCard(steps: List<String>) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEEF4FF))
            .border(1.dp, Color(0xFF90CAF9).copy(0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("📋  Steps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A6BB5))
        Spacer(Modifier.height(10.dp))
        steps.forEachIndexed { i, step ->
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 9.dp)) {
                Box( contentAlignment = Alignment.Center,
                    modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFF5E9BE0))
                ) {
                    Text("${i + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.width(9.dp))
                Text(step, fontSize = 13.sp, color = Color(0xFF3D2314), lineHeight = 19.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ListCard(title: String, items: List<String>, bg: Color, border: Color, text: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(bg)
            .border(1.dp, border.copy(0.3f), RoundedCornerShape(14.dp)).padding(12.dp)
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = text)
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            Text("• $item", fontSize = 12.sp, color = Color(0xFF3D2314), lineHeight = 17.sp, modifier = Modifier.padding(bottom = 5.dp))
        }
    }
}