package com.example.babyparenting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.viewmodel.JourneyViewModel

@Composable
fun SettingsScreen(
    viewModel: JourneyViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val childName     = viewModel.getChildName()
    val childAge      = viewModel.getChildAgeMonths()

    var nameInput     by remember { mutableStateOf(childName) }
    var emailInput    by remember { mutableStateOf("") }
    var saved         by remember { mutableStateOf(false) }
    var showLogout    by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0EB))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = Color(0xFF5C3D2E)
                )
            }
            Text(
                "Profile & Settings",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF2D1B0E)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Avatar / profile summary ──────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF8B94), Color(0xFFFFB06A))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text     = if (nameInput.isNotBlank()) nameInput.first().uppercase() else "P",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text       = if (nameInput.isNotBlank()) "$nameInput's Parent" else "Parent",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    if (childAge > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text    = "Child age: ${formatChildAge(childAge)}",
                            fontSize = 12.sp,
                            color   = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // ── Edit profile ──────────────────────────────────────────────────
            SectionLabel("Edit Profile")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingsTextField(
                    value         = nameInput,
                    onValueChange = { nameInput = it; saved = false },
                    label         = "Child's Name",
                    leadingIcon   = Icons.Default.ChildCare
                )
                SettingsTextField(
                    value         = emailInput,
                    onValueChange = { emailInput = it; saved = false },
                    label         = "Your Email (optional)",
                    leadingIcon   = Icons.Default.Email
                )

                Button(
                    onClick = {
                        viewModel.setChildName(nameInput.trim())
                        saved = true
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8B94)),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    if (saved) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Saved!", color = Color.White, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── App info ──────────────────────────────────────────────────────
            SectionLabel("About")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoRow("App Version", "1.0.0")
                InfoRow("Data Source", "15 child development datasets")
                InfoRow("Total Milestones", "76,000+ activities")
            }

            // ── Logout ────────────────────────────────────────────────────────
            SectionLabel("Account")

            Button(
                onClick = { showLogout = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp),
                    tint               = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text("Log Out", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Logout confirmation dialog ────────────────────────────────────────────
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            containerColor   = Color.White,
            shape            = RoundedCornerShape(16.dp),
            title = { Text("Log Out?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "You will be taken back to the login screen. Your progress is saved.",
                    fontSize = 13.sp,
                    color    = Color(0xFF555555)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLogout = false; onLogout() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Log Out", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(label: String) {
    Text(
        text       = label.uppercase(),
        fontSize   = 11.sp,
        fontWeight = FontWeight.Bold,
        color      = Color(0xFF9E9E9E),
        letterSpacing = 1.sp,
        modifier   = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        leadingIcon   = {
            Icon(leadingIcon, null, tint = Color(0xFFFF8B94), modifier = Modifier.size(18.dp))
        },
        singleLine = true,
        modifier   = Modifier.fillMaxWidth(),
        shape      = RoundedCornerShape(10.dp),
        colors     = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color(0xFFFF8B94),
            unfocusedBorderColor = Color(0xFFDDDDDD),
            focusedLabelColor    = Color(0xFFFF8B94),
            cursorColor          = Color(0xFFFF8B94)
        )
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF777777))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2D1B0E))
    }
}

private fun formatChildAge(months: Int): String = when {
    months == 0      -> "Newborn"
    months < 12      -> "$months month${if (months == 1) "" else "s"}"
    months == 12     -> "1 year"
    months % 12 == 0 -> "${months / 12} years"
    else             -> "${months / 12} yr ${months % 12} mo"
}
