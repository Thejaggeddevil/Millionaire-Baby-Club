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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.ParentGuide
import com.example.babyparenting.ui.theme.LocalAppColors

private val accentPalette = listOf(
    Color(0xFFFF8B94), Color(0xFFFFB06A), Color(0xFF9B8FD4),
    Color(0xFF4CAF82), Color(0xFF5E9BE0), Color(0xFFAB47BC)
)

@Composable
fun ParentGuideDetailScreen(guide: ParentGuide, onBack: () -> Unit) {
    val accent = accentPalette[guide.id.hashCode().and(0x7FFFFFFF) % accentPalette.size]

    Column(
        modifier = Modifier.fillMaxWidth().background(LocalAppColors.current.bgMain)
            .statusBarsPadding().navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(LocalAppColors.current.bgSurface)
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LocalAppColors.current.textSecondary)
            }
            Column(Modifier.weight(1f)) {
                Text(guide.skillName, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textPrimary, maxLines = 2)
                Text("${guide.domain.replaceFirstChar { it.uppercase() }}  ·  ${guide.ageGroupLabel}  ·  ${guide.ageRange}",
                    fontSize = 10.sp, color = LocalAppColors.current.textMuted)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero
            Box(
                modifier = Modifier.fillMaxWidth()
                    .shadow(5.dp, RoundedCornerShape(16.dp), ambientColor = accent.copy(0.25f), spotColor = accent.copy(0.25f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.68f))))
                    .padding(20.dp)
            ) {
                Column {
                    Text(guide.domain.trim().uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = Color.White.copy(0.75f), letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(guide.skillName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, lineHeight = 25.sp)
                    if (guide.learningGoal.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(guide.learningGoal, fontSize = 12.sp, color = Color.White.copy(0.85f), lineHeight = 17.sp)
                    }
                }
            }

            if (guide.whyItMatters.isNotBlank())
                DetailCard("Why it Matters", guide.whyItMatters, LocalAppColors.current.gold)
            if (guide.howToTeach.isNotBlank())
                DetailCard("How to Handle It", guide.howToTeach, LocalAppColors.current.sky)

            if (guide.dos.isNotEmpty() || guide.donts.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (guide.dos.isNotEmpty())   BulletBlock("Do",    guide.dos,   Color(0xFF4CAF82), Modifier.weight(1f))
                    if (guide.donts.isNotEmpty()) BulletBlock("Avoid", guide.donts, LocalAppColors.current.red,  Modifier.weight(1f))
                }
            }

            if (guide.tip.isNotBlank()) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(LocalAppColors.current.gold.copy(alpha = 0.10f))
                        .border(1.dp, LocalAppColors.current.yellow.copy(0.30f), RoundedCornerShape(12.dp)).padding(14.dp)
                ) {
                    Text("Quick Tip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.gold, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(guide.tip, fontSize = 13.sp, color = LocalAppColors.current.textPrimary, lineHeight = 19.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DetailCard(label: String, body: String, labelColor: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(LocalAppColors.current.bgSurface)
        .border(1.dp, labelColor.copy(0.22f), RoundedCornerShape(12.dp)).padding(14.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Text(body, fontSize = 14.sp, color = LocalAppColors.current.textPrimary, lineHeight = 21.sp)
    }
}

@Composable
private fun BulletBlock(label: String, items: List<String>, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(LocalAppColors.current.bgSurface)
        .border(1.dp, color.copy(0.22f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(8.dp))
        items.forEach { Text("• $it", fontSize = 12.sp, color = LocalAppColors.current.textPrimary,
            lineHeight = 17.sp, modifier = Modifier.padding(bottom = 4.dp)) }
    }
}
