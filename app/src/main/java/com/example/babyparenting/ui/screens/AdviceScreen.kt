package com.example.babyparenting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun AdviceScreen(
    viewModel: JourneyViewModel,
    onBack: () -> Unit
) {
    val adviceState       by viewModel.adviceState.collectAsState()
    val selectedMilestone by viewModel.selectedMilestone.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F0EB))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEEE0))
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF5C3D2E))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    selectedMilestone?.title ?: "Advice",
                    fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D1B0E), maxLines = 1
                )
                if (selectedMilestone != null) {
                    Text(
                        "${selectedMilestone!!.ageRange}  ·  ${selectedMilestone!!.source.displayName}",
                        fontSize = 11.sp, color = Color(0xFFAA8877)
                    )
                }
            }
        }

        // ── IMPORTANT: NO AnimatedContent here ───────────────────────────────
        // AnimatedContent gives unbounded height to children during transition.
        // fillMaxSize or fillMaxHeight inside AnimatedContent = CRASH.
        // Plain when{} is crash-safe.
        when (val state = adviceState) {
            is UiState.Loading -> LoadingView()
            is UiState.Success -> AdviceContent(
                advice    = state.data,
                milestone = selectedMilestone,
                onMarkDone = {
                    selectedMilestone?.let { viewModel.markComplete(it.id) }
                    viewModel.resetAdvice()
                    onBack()
                }
            )
            is UiState.Error -> ErrorView(state.message, state.retryable) { viewModel.retryAdvice() }
            is UiState.Idle  -> IdleView()
        }
    }
}

// ── State views ───────────────────────────────────────────────────────────────
// Use fillMaxWidth + padding only — never fillMaxSize inside unbounded containers

@Composable
private fun LoadingView() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(top = 100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFFFF8B94), strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(14.dp))
            Text("Getting advice…", fontSize = 14.sp, color = Color(0xFFAA8877))
        }
    }
}

@Composable
private fun IdleView() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(top = 100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text("Select a milestone to view advice", fontSize = 14.sp,
            color = Color(0xFFAA8877), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorView(message: String, retryable: Boolean, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFE57373), modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(12.dp))
        Text("Could not load advice", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D1B0E))
        Spacer(Modifier.height(6.dp))
        Text(message, fontSize = 13.sp, color = Color(0xFFAA8877), textAlign = TextAlign.Center)
        if (retryable) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8B94)), shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(15.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Try again", color = Color.White)
            }
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────
// fillMaxWidth + verticalScroll = SAFE (scroll only needs unbounded HEIGHT, not width)
// fillMaxSize + verticalScroll = CRASH (tries to fill infinite height)

@Composable
private fun AdviceContent(advice: AdviceResponse, milestone: Milestone?, onMarkDone: () -> Unit) {
    val accent = milestone?.let { Color(it.accentColor) } ?: Color(0xFFFF8B94)

    Column(
        modifier = Modifier
            .fillMaxWidth()                          // ← fillMaxWidth NOT fillMaxSize
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = accent.copy(0.25f), spotColor = accent.copy(0.25f))
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.72f))))
                .padding(20.dp)
        ) {
            Column {
                if (advice.domain.isNotBlank()) {
                    Text(advice.domain.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = Color.White.copy(0.78f), letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                }
                Text(advice.title.ifBlank { milestone?.title ?: "" },
                    fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 26.sp)
            }
        }

        if (advice.goal.isNotBlank())   AdviceCard("Goal",          advice.goal,    Color(0xFF5E9BE0))
        if (advice.why.isNotBlank())    AdviceCard("Why it Matters", advice.why,     Color(0xFFE6891A))
        if (advice.how.isNotBlank())    AdviceCard("How to Do It",  advice.how,     Color(0xFF4CAF82))
        if (advice.steps.isNotEmpty())  StepsCard(advice.steps)

        if (advice.dos.isNotEmpty() || advice.donts.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (advice.dos.isNotEmpty())   BulletCard("Do",    advice.dos,    Color(0xFF4CAF82), Modifier.weight(1f))
                if (advice.donts.isNotEmpty()) BulletCard("Avoid", advice.donts,  Color(0xFFE53935), Modifier.weight(1f))
            }
        }

        if (advice.tip.isNotBlank())     TipCard(advice.tip)
        if (advice.example.isNotBlank()) AdviceCard("Example", advice.example, Color(0xFF9C27B0))
        if (advice.answer.isNotBlank())  AdviceCard("Answer",  advice.answer,  Color(0xFF4CAF82))

        Spacer(Modifier.height(4.dp))

        // One-way complete button — always green, tapping goes back to journey
        Button(
            onClick  = onMarkDone,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF82))
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp), tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text("Mark as Complete", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun AdviceCard(label: String, body: String, labelColor: Color) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
            .border(1.dp, labelColor.copy(0.15f), RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Text(body, fontSize = 14.sp, color = Color(0xFF3D2314), lineHeight = 21.sp)
    }
}

@Composable
private fun StepsCard(steps: List<String>) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
            .border(1.dp, Color(0xFF90CAF9).copy(0.3f), RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Text("Steps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), letterSpacing = 0.5.sp)
        Spacer(Modifier.height(10.dp))
        steps.forEachIndexed { i, step ->
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 9.dp)) {
                Box(Alignment.Center as Modifier,
                    Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFFF8B94)) as Alignment
                ) {
                    Text("${i + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Text(step, fontSize = 13.sp, color = Color(0xFF3D2314), lineHeight = 19.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BulletCard(label: String, items: List<String>, labelColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color.White)
            .border(1.dp, labelColor.copy(0.2f), RoundedCornerShape(14.dp)).padding(12.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(7.dp))
        items.take(5).forEach { item ->
            Text("• $item", fontSize = 12.sp, color = Color(0xFF3D2314), lineHeight = 17.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
        if (items.size > 5) Text("+${items.size - 5} more", fontSize = 11.sp, color = Color(0xFFBBBBBB))
    }
}

@Composable
private fun TipCard(tip: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFF8E1))
            .border(1.dp, Color(0xFFFFD600).copy(0.35f), RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Text("Quick Tip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B6D00), letterSpacing = 0.5.sp)
        Spacer(Modifier.height(5.dp))
        Text(tip, fontSize = 13.sp, color = Color(0xFF4A3800), lineHeight = 19.sp)
    }
}