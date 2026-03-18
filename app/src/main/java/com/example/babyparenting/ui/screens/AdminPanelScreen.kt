package com.example.babyparenting.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babyparenting.data.model.AdminMilestone
import com.example.babyparenting.data.model.AgeGroup
import com.example.babyparenting.viewmodel.AdminAuthState
import com.example.babyparenting.viewmodel.AdminPanelState
import com.example.babyparenting.viewmodel.AdminViewModel

@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val authState  by viewModel.authState.collectAsState()
    val panelState by viewModel.panelState.collectAsState()
    val toastMsg   by viewModel.toastMessage.collectAsState()

    // Show toast
    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearToast()
        }
    }

    when (authState) {
        is AdminAuthState.LoggedOut,
        is AdminAuthState.Error -> AdminLoginScreen(viewModel = viewModel, onBack = onBack)
        is AdminAuthState.LoggedIn -> {
            AnimatedContent(
                targetState = panelState,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it } + fadeOut())
                },
                label = "adminPanel"
            ) { state ->
                when (state) {
                    is AdminPanelState.List           -> AdminListScreen(viewModel, onBack, toastMsg)
                    is AdminPanelState.AddEdit        -> AdminFormScreen(viewModel, state.milestone)
                    is AdminPanelState.ConfirmDelete  -> ConfirmDeleteDialog(
                        milestone  = state.milestone,
                        onConfirm  = { viewModel.confirmDelete(state.milestone.id) },
                        onDismiss  = { viewModel.backToList() }
                    )
                    is AdminPanelState.ChangePassword -> ChangePasswordScreen(viewModel)
                }
            }
        }
    }
}

// ── Login ─────────────────────────────────────────────────────────────────────

@Composable
private fun AdminLoginScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val authState by viewModel.authState.collectAsState()
    var password  by remember { mutableStateOf("") }
    var showPwd   by remember { mutableStateOf(false) }
    var attempts  by remember { mutableStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0EB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF1565C0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Admin Panel", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
        Text("Enter your admin password", fontSize = 13.sp, color = Color(0xFF9E9E9E))

        if (attempts >= 3) {
            Spacer(Modifier.height(8.dp))
            Text("Hint: default password is admin123", fontSize = 11.sp, color = Color(0xFFE57373))
        }

        if (authState is AdminAuthState.Error) {
            Spacer(Modifier.height(8.dp))
            Text((authState as AdminAuthState.Error).message, fontSize = 12.sp, color = Color(0xFFE53935))
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value         = password,
            onValueChange = { password = it },
            label         = { Text("Password") },
            singleLine    = true,
            visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPwd = !showPwd }) {
                    Icon(if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            colors   = adminFieldColors()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                attempts++
                viewModel.login(password)
                if (authState is AdminAuthState.Error) password = ""
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape    = RoundedCornerShape(12.dp)
        ) { Text("Login", fontSize = 15.sp, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) {
            Text("← Back to App", color = Color(0xFF9E9E9E))
        }
    }
}

// ── List screen ───────────────────────────────────────────────────────────────

@Composable
private fun AdminListScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    toastMsg: String?
) {
    val milestones by viewModel.milestones.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F0EB),
        topBar = {
            Column(
                Modifier.fillMaxWidth().background(Color(0xFF1565C0)).statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Admin Panel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${milestones.size} custom milestone${if (milestones.size == 1) "" else "s"}",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = { viewModel.openChangePassword() }) {
                        Icon(Icons.Default.Lock, null, tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.openAddNew() },
                containerColor = Color(0xFF1565C0)
            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
        }
    ) { padding ->

        if (milestones.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No custom milestones yet", fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold, color = Color(0xFF555555))
                    Spacer(Modifier.height(6.dp))
                    Text("Tap + to add a new milestone", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                    Spacer(Modifier.height(4.dp))
                    Text("CSV milestones are loaded automatically", fontSize = 11.sp, color = Color(0xFFBBBBBB))
                }
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(padding),
                contentPadding  = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(milestones, key = { it.id }) { m ->
                    AdminMilestoneCard(
                        milestone = m,
                        onEdit    = { viewModel.openEdit(m) },
                        onDelete  = { viewModel.openConfirmDelete(m) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Toast overlay
    if (toastMsg != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(
                Modifier
                    .padding(bottom = 80.dp, start = 24.dp, end = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A2E))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(toastMsg, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

// ── Milestone card in list ────────────────────────────────────────────────────

@Composable
private fun AdminMilestoneCard(
    milestone: AdminMilestone,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier   = Modifier.fillMaxWidth(),
        shape      = RoundedCornerShape(12.dp),
        colors     = CardDefaults.cardColors(containerColor = Color.White),
        elevation  = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF1565C0).copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) { Text(milestone.iconEmoji, fontSize = 18.sp) }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(milestone.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${milestone.ageRange}  •  ${milestone.domain}", fontSize = 10.sp,
                    color = Color(0xFF9E9E9E), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Add / Edit form ───────────────────────────────────────────────────────────

@Composable
private fun AdminFormScreen(viewModel: AdminViewModel, existing: AdminMilestone?) {
    val ageGroups   = viewModel.ageGroups()
    val isNew       = existing == null

    var title         by remember { mutableStateOf(existing?.title ?: "") }
    var subtitle      by remember { mutableStateOf(existing?.subtitle ?: "") }
    var domain        by remember { mutableStateOf(existing?.domain ?: "") }
    var ageMonths     by remember { mutableStateOf(existing?.ageMonths?.toString() ?: "0") }
    var ageRange      by remember { mutableStateOf(existing?.ageRange ?: "") }
    var apiQuery      by remember { mutableStateOf(existing?.apiQuery ?: "") }
    var iconEmoji     by remember { mutableStateOf(existing?.iconEmoji ?: "⭐") }
    var groupId       by remember { mutableStateOf(existing?.ageGroupId ?: 1) }
    var showDropdown  by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5F0EB)).statusBarsPadding().navigationBarsPadding()
    ) {
        // App bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = { viewModel.backToList() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Text(
                if (isNew) "Add New Milestone" else "Edit Milestone",
                fontSize = 17.sp, fontWeight = FontWeight.Bold,
                color = Color.White, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val error = viewModel.save(
                    AdminMilestone(
                        id         = existing?.id ?: "",
                        title      = title.trim(),
                        subtitle   = subtitle.trim(),
                        domain     = domain.trim().ifBlank { ageGroups.find { it.id == groupId }?.label ?: "General" },
                        ageMonths  = ageMonths.toIntOrNull() ?: 0,
                        ageRange   = ageRange.trim().ifBlank { "${ageMonths.toIntOrNull() ?: 0} mo" },
                        ageGroupId = groupId,
                        apiQuery   = apiQuery.trim(),
                        iconEmoji  = iconEmoji.ifBlank { "⭐" }
                    )
                )
                errorMessage = error
            }) {
                Icon(Icons.Default.Save, null, tint = Color.White)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorMessage != null) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFEBEE)).padding(12.dp)
                ) {
                    Text("⚠️  $errorMessage", fontSize = 13.sp, color = Color(0xFFC62828))
                }
            }

            AdminFormField("Title *", title, { title = it }, hint = "e.g. First Words")
            AdminFormField("Subtitle", subtitle, { subtitle = it }, hint = "e.g. Mama & dada with meaning")
            AdminFormField("Domain / Category", domain, { domain = it }, hint = "e.g. Language, Motor, Safety")
            AdminFormField("Emoji Icon", iconEmoji, { iconEmoji = it }, hint = "e.g. 🗣️")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminFormField("Age (months)", ageMonths, { ageMonths = it },
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                AdminFormField("Age Range label", ageRange, { ageRange = it },
                    hint = "e.g. 12 mo", modifier = Modifier.weight(1f))
            }

            AdminFormField(
                label     = "Backend Query *",
                value     = apiQuery,
                onChange  = { apiQuery = it },
                hint      = "e.g. baby first words language development 12 months",
                singleLine = false,
                minLines  = 2
            )

            // Age Group dropdown
            Text("Age Group *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(10.dp))
                        .clickable { showDropdown = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    val sel = ageGroups.find { it.id == groupId }
                    Box(Modifier.size(10.dp).clip(CircleShape).background(sel?.let { Color(it.accentColor) } ?: Color.Gray))
                    Spacer(Modifier.width(10.dp))
                    Text(sel?.label ?: "Select group", fontSize = 14.sp, color = Color(0xFF1A1A2E), modifier = Modifier.weight(1f))
                    Text("▾", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                }
                DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                    ageGroups.forEach { g ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(g.accentColor)))
                                    Spacer(Modifier.width(8.dp))
                                    Text(g.label, fontSize = 13.sp)
                                }
                            },
                            onClick = { groupId = g.id; showDropdown = false }
                        )
                    }
                }
            }

            // Tip box
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE3F2FD)).padding(12.dp)
            ) {
                Text(
                    "💡 The Backend Query is sent to POST /predict on your server. " +
                    "Make it descriptive so the model returns relevant advice.\n" +
                    "Example: \"child reading aloud phonics 5 years school\"",
                    fontSize = 11.sp, color = Color(0xFF1565C0), lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Change password ───────────────────────────────────────────────────────────

@Composable
private fun ChangePasswordScreen(viewModel: AdminViewModel) {
    var current    by remember { mutableStateOf("") }
    var newPwd     by remember { mutableStateOf("") }
    var confirm    by remember { mutableStateOf("") }
    var showPwd    by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5F0EB)).statusBarsPadding().navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = { viewModel.backToList() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Text("Change Admin Password", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorMsg != null) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFEBEE)).padding(12.dp)) {
                    Text("⚠️ $errorMsg", fontSize = 13.sp, color = Color(0xFFC62828))
                }
            }

            val vis = if (showPwd) VisualTransformation.None else PasswordVisualTransformation()

            OutlinedTextField(
                value = current, onValueChange = { current = it; errorMsg = null },
                label = { Text("Current password") }, singleLine = true,
                visualTransformation = vis,
                trailingIcon = { IconButton(onClick = { showPwd = !showPwd }) {
                    Icon(if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) }},
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = adminFieldColors()
            )
            OutlinedTextField(
                value = newPwd, onValueChange = { newPwd = it; errorMsg = null },
                label = { Text("New password (min 6 chars)") }, singleLine = true,
                visualTransformation = vis,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = adminFieldColors()
            )
            OutlinedTextField(
                value = confirm, onValueChange = { confirm = it; errorMsg = null },
                label = { Text("Confirm new password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = adminFieldColors()
            )

            Button(
                onClick = {
                    errorMsg = viewModel.changePassword(current, newPwd, confirm)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape    = RoundedCornerShape(12.dp)
            ) { Text("Save New Password", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// ── Confirm delete dialog ─────────────────────────────────────────────────────

@Composable
private fun ConfirmDeleteDialog(
    milestone: AdminMilestone,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(16.dp),
        title = { Text("Delete milestone?", fontWeight = FontWeight.Bold) },
        text  = { Text("\"${milestone.title}\" will be permanently removed.", fontSize = 13.sp, color = Color(0xFF555555)) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Reusable form field ───────────────────────────────────────────────────────

@Composable
private fun AdminFormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    hint: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value           = value,
            onValueChange   = onChange,
            singleLine      = singleLine,
            minLines        = minLines,
            placeholder     = if (hint.isNotEmpty()) {{ Text(hint, color = Color(0xFFBDBDBD)) }} else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(10.dp),
            colors          = adminFieldColors()
        )
    }
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Color(0xFF1565C0),
    unfocusedBorderColor = Color(0xFFBDBDBD),
    focusedLabelColor    = Color(0xFF1565C0),
    cursorColor          = Color(0xFF1565C0)
)
