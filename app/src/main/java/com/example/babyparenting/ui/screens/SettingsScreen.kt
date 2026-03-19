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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Email
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
import com.example.babyparenting.ui.theme.LocalAppColors
import com.example.babyparenting.viewmodel.JourneyViewModel

@Composable
fun SettingsScreen(viewModel: JourneyViewModel, onBack: () -> Unit, onLogout: () -> Unit) {
    val childName  = viewModel.getChildName()
    val childAge   = viewModel.getChildAgeMonths()
    var nameInput  by remember { mutableStateOf(childName) }
    var emailInput by remember { mutableStateOf("") }
    var saved      by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().background(LocalAppColors.current.bgMain)
            .statusBarsPadding().navigationBarsPadding()
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
            Text("Profile & Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textPrimary)
        }

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar card
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(LocalAppColors.current.coral, LocalAppColors.current.peach)))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Text(
                            text = if (nameInput.isNotBlank()) nameInput.first().uppercase() else "P",
                            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (nameInput.isNotBlank()) "$nameInput's Parent" else "Parent",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                    if (childAge > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text("Child age: ${formatChildAge(childAge)}", fontSize = 12.sp, color = Color.White.copy(0.85f))
                    }
                }
            }

            SectionLabel("Edit Profile")

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(LocalAppColors.current.bgSurface).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingsField(nameInput, { nameInput = it; saved = false }, "Child's Name", Icons.Default.ChildCare)
                SettingsField(emailInput, { emailInput = it; saved = false }, "Your Email (optional)", Icons.Default.Email)

                Button(
                    onClick = { viewModel.setChildName(nameInput.trim()); saved = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.coral),
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

            SectionLabel("About")

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(LocalAppColors.current.bgSurface).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoRow("App Version", "1.0.0")
                InfoRow("Data Source", "15 child development datasets")
                InfoRow("Total Milestones", "76,000+ activities")
            }

            SectionLabel("Account")

            Button(
                onClick = { showLogout = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.red),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Log Out", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            containerColor   = LocalAppColors.current.bgElevated,
            shape            = RoundedCornerShape(16.dp),
            title = { Text("Log Out?", fontWeight = FontWeight.Bold, color = LocalAppColors.current.textPrimary) },
            text  = { Text("You will be taken back to the login screen. Your progress is saved.",
                fontSize = 13.sp, color = LocalAppColors.current.textSecondary) },
            confirmButton = {
                Button(onClick = { showLogout = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.red)) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Cancel", color = LocalAppColors.current.textMuted) } }
        )
    }
}

@Composable private fun SectionLabel(label: String) {
    Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.textMuted, letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp))
}

@Composable private fun SettingsField(value: String, onChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = LocalAppColors.current.coral, modifier = Modifier.size(18.dp)) },
        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LocalAppColors.current.coral, unfocusedBorderColor = LocalAppColors.current.border,
            focusedLabelColor = LocalAppColors.current.coral, unfocusedLabelColor = LocalAppColors.current.textMuted,
            focusedTextColor = LocalAppColors.current.textPrimary, unfocusedTextColor = LocalAppColors.current.textPrimary,
            focusedContainerColor = LocalAppColors.current.bgSurface, unfocusedContainerColor = LocalAppColors.current.bgSurface,
            cursorColor = LocalAppColors.current.coral
        )
    )
}

@Composable private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LocalAppColors.current.textPrimary)
    }
}

private fun formatChildAge(months: Int): String = when {
    months == 0      -> "Newborn"
    months < 12      -> "$months month${if (months == 1) "" else "s"}"
    months == 12     -> "1 year"
    months % 12 == 0 -> "${months / 12} years"
    else             -> "${months / 12} yr ${months % 12} mo"
}
