package com.example.babyparenting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.babyparenting.data.local.AdminMilestoneStore
import com.example.babyparenting.data.model.AdminMilestone
import com.example.babyparenting.data.model.AgeGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AdminAuthState {
    object LoggedOut                            : AdminAuthState()
    object LoggedIn                             : AdminAuthState()
    data class Error(val message: String)       : AdminAuthState()
}

sealed class AdminPanelState {
    object List                                              : AdminPanelState()
    data class AddEdit(val milestone: AdminMilestone?)       : AdminPanelState()
    data class ConfirmDelete(val milestone: AdminMilestone)  : AdminPanelState()
    object ChangePassword                                    : AdminPanelState()
}

class AdminViewModel(app: Application) : AndroidViewModel(app) {

    private val store = AdminMilestoneStore(app)

    private val _authState  = MutableStateFlow<AdminAuthState>(AdminAuthState.LoggedOut)
    val authState: StateFlow<AdminAuthState> = _authState.asStateFlow()

    private val _panelState = MutableStateFlow<AdminPanelState>(AdminPanelState.List)
    val panelState: StateFlow<AdminPanelState> = _panelState.asStateFlow()

    private val _milestones = MutableStateFlow<List<AdminMilestone>>(emptyList())
    val milestones: StateFlow<List<AdminMilestone>> = _milestones.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init { refreshList() }

    // ── Auth ──────────────────────────────────────────────────────────────────

    /** Called from AdminPanelScreen internal login form */
    fun login(password: String) {
        if (store.checkPassword(password)) {
            _authState.value = AdminAuthState.LoggedIn
        } else {
            _authState.value = AdminAuthState.Error("Incorrect password")
        }
    }

    /** Called from LoginScreen — returns true/false for navigation decision */
    fun loginFromStart(password: String): Boolean {
        return if (store.checkPassword(password)) {
            _authState.value = AdminAuthState.LoggedIn
            true
        } else {
            false
        }
    }

    fun logout() {
        _authState.value  = AdminAuthState.LoggedOut
        _panelState.value = AdminPanelState.List
    }

    fun changePassword(current: String, newPwd: String, confirm: String): String? {
        return when {
            !store.checkPassword(current) -> "Current password is incorrect"
            newPwd.length < 6            -> "New password must be at least 6 characters"
            newPwd != confirm            -> "Passwords do not match"
            else -> {
                store.setAdminPassword(newPwd)
                _panelState.value   = AdminPanelState.List
                _toastMessage.value = "Password changed"
                null
            }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    fun openAddNew()                          { _panelState.value = AdminPanelState.AddEdit(null) }
    fun openEdit(m: AdminMilestone)           { _panelState.value = AdminPanelState.AddEdit(m) }
    fun openConfirmDelete(m: AdminMilestone)  { _panelState.value = AdminPanelState.ConfirmDelete(m) }
    fun openChangePassword()                  { _panelState.value = AdminPanelState.ChangePassword }
    fun backToList()                          { _panelState.value = AdminPanelState.List }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun save(milestone: AdminMilestone): String? {
        if (milestone.title.isBlank())    return "Title is required"
        if (milestone.apiQuery.isBlank()) return "Backend query is required"

        if (milestone.id.isBlank() || store.getById(milestone.id) == null) {
            store.add(milestone)
            _toastMessage.value = "Milestone added"
        } else {
            store.update(milestone)
            _toastMessage.value = "Milestone updated"
        }
        refreshList()
        _panelState.value = AdminPanelState.List
        return null
    }

    fun confirmDelete(id: String) {
        store.delete(id)
        _toastMessage.value = "Milestone deleted"
        refreshList()
        _panelState.value = AdminPanelState.List
    }

    fun clearToast() { _toastMessage.value = null }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun refreshList() { _milestones.value = store.getAll() }

    fun ageGroups(): List<AgeGroup> = listOf(
        AgeGroup(1,  "0 – 3 Months",   "", 0,   3,   0xFFFF8B94),
        AgeGroup(2,  "3 – 6 Months",   "", 3,   6,   0xFFFFB347),
        AgeGroup(3,  "6 – 9 Months",   "", 6,   9,   0xFFFFC75F),
        AgeGroup(4,  "9 – 12 Months",  "", 9,   12,  0xFF98D8C8),
        AgeGroup(5,  "1 – 1.5 Years",  "", 12,  18,  0xFFB5EAD7),
        AgeGroup(6,  "1.5 – 2 Years",  "", 18,  24,  0xFF7C83FD),
        AgeGroup(7,  "2 – 3 Years",    "", 24,  36,  0xFFADD8E6),
        AgeGroup(8,  "3 – 4 Years",    "", 36,  48,  0xFFD4A5F5),
        AgeGroup(9,  "4 – 5 Years",    "", 48,  60,  0xFFFDDB92),
        AgeGroup(10, "5 – 7 Years",    "", 60,  84,  0xFF66BB6A),
        AgeGroup(11, "7 – 9 Years",    "", 84,  108, 0xFF42A5F5),
        AgeGroup(12, "9 – 11 Years",   "", 108, 132, 0xFFFF6B6B),
        AgeGroup(13, "11 – 12 Years",  "", 132, 144, 0xFF26C6DA)
    )
}