package com.example.babyparenting.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    onComplete: (name: String, ageMonths: Int) -> Unit
) {
    var childName  by remember { mutableStateOf("") }
    var sliderVal  by remember { mutableStateOf(0f) }
    var nameError  by remember { mutableStateOf(false) }
    val months = sliderVal.roundToInt()

    val logoScale = remember { Animatable(0f) }
    val pageAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        pageAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF0E4), Color(0xFFFFDDC8), Color(0xFFFAF6F1))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .alpha(pageAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .scale(logoScale.value)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFF8B94), Color(0xFFFFB347)))
                    )
            ) {
                Icon(
                    Icons.Default.ChildCare,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Welcome!",
                fontSize   = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color(0xFF2D1B0E)
            )
            Text(
                "Baby Parenting Companion",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = Color(0xFFFF8B94)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Powered by 76,000+ child development activities",
                fontSize   = 12.sp,
                color      = Color(0xFF9E9E9E),
                fontStyle  = FontStyle.Italic,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // ── Form card ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Set up your child's profile",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF2D1B0E)
                )

                // Child name field
                OutlinedTextField(
                    value         = childName,
                    onValueChange = { childName = it; nameError = false },
                    label         = { Text("Child's name *") },
                    placeholder   = { Text("e.g. Arjun", color = Color(0xFFBDBDBD)) },
                    leadingIcon   = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint     = Color(0xFFFF8B94),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine    = true,
                    isError       = nameError,
                    supportingText = if (nameError) {
                        { Text("Please enter your child's name", color = Color(0xFFE53935), fontSize = 11.sp) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Color(0xFFFF8B94),
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedLabelColor    = Color(0xFFFF8B94),
                        cursorColor          = Color(0xFFFF8B94)
                    )
                )

                // Age slider
                Column {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Child's current age",
                            fontSize = 13.sp,
                            color    = Color(0xFF555555)
                        )
                        Text(
                            formatAge(months),
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color(0xFF1565C0)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
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
                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween
                    ) {
                        Text("Newborn", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                        Text("12 years", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                    }
                    if (months > 0) {
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(10.dp)
                        ) {
                            Text(
                                "✓ All milestones up to ${formatAge(months)} will be auto-completed",
                                fontSize = 11.sp,
                                color    = Color(0xFF1565C0)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Privacy note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE3F2FD))
                    .padding(12.dp)
            ) {
                Text("🔒 ", fontSize = 13.sp)
                Text(
                    "Your data stays on this device. Connect Firebase later to sync across devices.",
                    fontSize   = 11.sp,
                    color      = Color(0xFF5C7A9E),
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Dataset summary card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "What's included:",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF2D1B0E)
                )
                listOf(
                    "🍼 0–24 months: Baby activities & parent guides",
                    "🎠 2–5 years: Toddler activities & pre-academics",
                    "🛡️ Safety & body awareness (ages 4–12)",
                    "📚 School: Maths, Science, Social Studies",
                    "🏛️ Civics, Computer Science & Foreign Languages",
                    "🗣️ Language & Communication skills"
                ).forEach { item ->
                    Text(item, fontSize = 11.sp, color = Color(0xFF555555))
                }
            }

            Spacer(Modifier.height(24.dp))

            // CTA button
            Button(
                onClick = {
                    if (childName.isBlank()) {
                        nameError = true
                    } else {
                        onComplete(childName.trim(), months)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8B94))
            ) {
                Text(
                    "Start the Journey ✨",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "By continuing you agree to our Terms of Service.",
                fontSize = 10.sp,
                color    = Color(0xFFBBBBBB)
            )
        }
    }
}

private fun formatAge(months: Int): String = when {
    months == 0      -> "Newborn"
    months < 12      -> "$months month${if (months == 1) "" else "s"}"
    months == 12     -> "1 year"
    months % 12 == 0 -> "${months / 12} years"
    else             -> "${months / 12} yr ${months % 12} mo"
}
