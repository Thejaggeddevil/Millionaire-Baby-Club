package com.example.babyparenting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.ParentGuide
import com.example.babyparenting.ui.theme.LocalAppColors
import com.example.babyparenting.viewmodel.ParentViewModel

private val domainColors = listOf(
    Color(0xFFFF8B94), Color(0xFFFFB06A), Color(0xFFFFC75F),
    Color(0xFF98D8C8), Color(0xFF9B8FD4), Color(0xFF7C83FD),
    Color(0xFF5E9BE0), Color(0xFFD4A5F5), Color(0xFFFDDB92),
    Color(0xFF4CAF82), Color(0xFF42A5F5), Color(0xFFAB47BC)
)

@Composable
fun ParentHubScreen(viewModel: ParentViewModel, onBack: () -> Unit, onGuideSelected: (ParentGuide) -> Unit) {
    val results        by viewModel.results.collectAsState()
    val query          by viewModel.searchQuery.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val error          by viewModel.error.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    val selectedAge    by viewModel.selectedAgeGroup.collectAsState()
    val domains        by viewModel.domains.collectAsState()

    Column(
        Modifier.fillMaxWidth().background(LocalAppColors.current.bgMain).statusBarsPadding().navigationBarsPadding()
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(LocalAppColors.current.bgSurface)
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LocalAppColors.current.textSecondary)
            }
            Column(Modifier.weight(1f)) {
                Text("Parent Hub", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LocalAppColors.current.textPrimary)
                Text("Search guides for any parenting challenge", fontSize = 11.sp, color = LocalAppColors.current.textSecondary)
            }
        }

        // Search bar
        OutlinedTextField(
            value = query, onValueChange = { viewModel.onSearchQuery(it) },
            placeholder = { Text("e.g. picky eating, tantrums, screen time…", fontSize = 13.sp, color = LocalAppColors.current.textMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFFF8B94), modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (query.isNotBlank()) IconButton(onClick = { viewModel.onSearchQuery("") }) {
                    Icon(Icons.Default.Close, null, tint = LocalAppColors.current.textMuted, modifier = Modifier.size(18.dp))
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {}),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color(0xFFFF8B94),
                unfocusedBorderColor    = LocalAppColors.current.border,
                focusedContainerColor   = LocalAppColors.current.bgSurface,
                unfocusedContainerColor = LocalAppColors.current.bgSurface,
                focusedTextColor        = LocalAppColors.current.textPrimary,
                unfocusedTextColor      = LocalAppColors.current.textPrimary,
                cursorColor             = LocalAppColors.current.coral
            )
        )

        // Age group chips
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.ageGroups.forEach { age ->
                val sel = selectedAge == age
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (sel) LocalAppColors.current.coral else LocalAppColors.current.bgSurface)
                        .border(1.dp, LocalAppColors.current.coral.copy(alpha = if (sel) 0f else 0.35f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.onAgeGroupSelected(if (sel) null else age) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(age, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        color = if (sel) Color.White else LocalAppColors.current.textSecondary)
                }
            }
        }

        // Domain chips
        if (domains.isNotEmpty()) {
            LazyRow(contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                item {
                    DomainChip("All", selectedDomain == null, LocalAppColors.current.lavender) { viewModel.onDomainSelected(null) }
                }
                items(domains) { domain ->
                    val color = domainColors[domains.indexOf(domain) % domainColors.size]
                    DomainChip(domain, selectedDomain == domain, color) {
                        viewModel.onDomainSelected(if (selectedDomain == domain) null else domain)
                    }
                }
            }
        }

        // Count row
        if (!isLoading && error == null) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${results.size} guide${if (results.size == 1) "" else "s"} found",
                    fontSize = 12.sp, color = LocalAppColors.current.textMuted)
                if (query.isNotBlank() || selectedDomain != null || selectedAge != null) {
                    Spacer(Modifier.width(8.dp))
                    Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF8B94), modifier = Modifier.clickable { viewModel.clearFilters() })
                }
            }
        }

        // Content
        when {
            isLoading -> Box(Modifier.fillMaxWidth().padding(top = 80.dp), Alignment.TopCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFF8B94), strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Loading parent guides…", fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
                }
            }
            error != null -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), Alignment.TopCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("Could not load guides", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textPrimary)
                    Spacer(Modifier.height(6.dp)); Text(error!!, fontSize = 12.sp, color = LocalAppColors.current.textSecondary, textAlign = TextAlign.Center)
                }
            }
            results.isEmpty() -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), Alignment.TopCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("No guides found", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(if (query.isNotBlank()) "Try different words or clear filters" else "Try a different age group",
                        fontSize = 12.sp, color = LocalAppColors.current.textSecondary, textAlign = TextAlign.Center)
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results.take(150), key = { it.id }) { guide ->
                    val color = domainColors[domains.indexOf(guide.domain.trim().lowercase().replaceFirstChar { it.uppercase() }) % domainColors.size]
                    GuideCard(guide, color) { onGuideSelected(guide) }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun DomainChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(if (selected) color else LocalAppColors.current.bgSurface)
            .border(1.dp, color.copy(alpha = if (selected) 0f else 0.35f), RoundedCornerShape(20.dp))
            .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else LocalAppColors.current.textSecondary, maxLines = 1)
    }
}

@Composable
private fun GuideCard(guide: ParentGuide, accentColor: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(LocalAppColors.current.bgSurface)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)).clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(accentColor))
            Spacer(Modifier.width(7.dp))
            Text(guide.domain.trim().replaceFirstChar { it.uppercase() }, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, color = accentColor)
            Spacer(Modifier.weight(1f))
            Text("${guide.ageGroupLabel}  ·  ${guide.ageRange}", fontSize = 10.sp, color = LocalAppColors.current.textMuted)
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(guide.skillName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = LocalAppColors.current.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (guide.whyItMatters.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(guide.whyItMatters, fontSize = 12.sp, color = LocalAppColors.current.textSecondary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
            }
            if (guide.tip.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(LocalAppColors.current.gold.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("Tip", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.gold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(guide.tip, fontSize = 11.sp, color = LocalAppColors.current.textSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
