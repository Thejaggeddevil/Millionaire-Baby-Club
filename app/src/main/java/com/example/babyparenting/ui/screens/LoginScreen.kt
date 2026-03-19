package com.example.babyparenting.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onParentLogin: () -> Unit,
    onAdminLogin: (password: String) -> Boolean  // returns true if password correct
) {
    var showAdminForm by remember { mutableStateOf(false) }
    var password      by remember { mutableStateOf("") }
    var showPwd       by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var attempts      by remember { mutableStateOf(0) }

    val logoScale = remember { Animatable(0f) }
    val pageAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        pageAlpha.animateTo(1f, tween(400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF0E6), Color(0xFFFFE4D0), Color(0xFFF5F0EB))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .alpha(pageAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .scale(logoScale.value)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF8B94), Color(0xFFFFB06A))
                        )
                    )
            ) {
                Text("👶", fontSize = 38.sp)
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "Baby Parenting Companion",
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color(0xFF2D1B0E),
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Track every milestone with love",
                fontSize  = 13.sp,
                color     = Color(0xFFAA8877),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(44.dp))

            if (!showAdminForm) {
                // ── Choose role ───────────────────────────────────────────────

                Text(
                    "Who is using the app?",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF5C3D2E)
                )

                Spacer(Modifier.height(20.dp))

                // Parent card
                RoleCard(
                    emoji       = "👨‍👩‍👧",
                    title       = "I'm a Parent",
                    subtitle    = "Track my child's milestones",
                    bgColor     = Color(0xFFFF8B94),
                    borderColor = Color(0xFFFF8B94),
                    onClick     = onParentLogin
                )

                Spacer(Modifier.height(14.dp))

                // Admin card
                RoleCard(
                    emoji       = "🛡️",
                    title       = "Admin Login",
                    subtitle    = "Manage & add milestones",
                    bgColor     = Color(0xFF1565C0),
                    borderColor = Color(0xFF1565C0),
                    onClick     = { showAdminForm = true }
                )

            } else {
                // ── Admin password form ───────────────────────────────────────

                Box(
                    Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1565C0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Admin Login",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A2E)
                )
                Text(
                    "Enter your admin password to continue",
                    fontSize  = 13.sp,
                    color     = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center
                )

                if (attempts >= 3) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Hint: default password is admin123",
                        fontSize = 11.sp,
                        color    = Color(0xFFE57373)
                    )
                }

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value         = password,
                    onValueChange = { password = it; passwordError = false },
                    label         = { Text("Password") },
                    singleLine    = true,
                    isError       = passwordError,
                    supportingText = if (passwordError) {
                        { Text("Incorrect password. Try again.", color = Color(0xFFE53935), fontSize = 11.sp) }
                    } else null,
                    visualTransformation = if (showPwd) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPwd = !showPwd }) {
                            Icon(
                                if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null,
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedLabelColor    = Color(0xFF1565C0),
                        cursorColor          = Color(0xFF1565C0)
                    )
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = {
                        val ok = onAdminLogin(password)
                        if (!ok) {
                            passwordError = true
                            attempts++
                            password = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Login as Admin", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = {
                    showAdminForm = false
                    password      = ""
                    passwordError = false
                }) {
                    Text("← Back", color = Color(0xFF9E9E9E), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    emoji: String,
    title: String,
    subtitle: String,
    bgColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, borderColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(bgColor.copy(alpha = 0.12f))
        ) {
            Text(emoji, fontSize = 22.sp)
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1A1A2E)
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color    = Color(0xFF9E9E9E)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bgColor)
        ) {
            Text("→", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
