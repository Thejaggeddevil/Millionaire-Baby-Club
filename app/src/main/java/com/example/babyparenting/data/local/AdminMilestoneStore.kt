package com.example.babyparenting.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.babyparenting.data.model.AdminMilestone
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * Persists admin-added milestones locally in SharedPreferences using Gson.
 *
 * Rules:
 *  - CSV milestones are READ-ONLY — they cannot be deleted or edited here.
 *  - Only milestones created through the admin panel are stored here.
 *  - These can be created, updated, and deleted freely.
 *
 * Firebase-ready: replace SharedPreferences calls with Firestore collection
 * when you add Firebase to the project.
 */
class AdminMilestoneStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("admin_milestones", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ── Read ──────────────────────────────────────────────────────────────────

    fun getAll(): List<AdminMilestone> {
        val json = prefs.getString(KEY_MILESTONES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AdminMilestone>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun getById(id: String): AdminMilestone? = getAll().find { it.id == id }

    // ── Write ─────────────────────────────────────────────────────────────────

    fun add(milestone: AdminMilestone): AdminMilestone {
        val withId = if (milestone.id.isBlank())
            milestone.copy(id = "admin_${UUID.randomUUID()}")
        else milestone
        val updated = getAll().toMutableList().apply { add(withId) }
        saveAll(updated)
        return withId
    }

    fun update(milestone: AdminMilestone) {
        val updated = getAll().map { if (it.id == milestone.id) milestone else it }
        saveAll(updated)
    }

    fun delete(id: String) {
        val updated = getAll().filter { it.id != id }
        saveAll(updated)
    }

    fun deleteAll() {
        prefs.edit().remove(KEY_MILESTONES).apply()
    }

    // ── Admin auth ────────────────────────────────────────────────────────────

    fun getAdminPassword(): String =
        prefs.getString(KEY_ADMIN_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD

    fun setAdminPassword(newPassword: String) {
        prefs.edit().putString(KEY_ADMIN_PASSWORD, newPassword).apply()
    }

    fun checkPassword(input: String): Boolean = input == getAdminPassword()

    // ── Private ───────────────────────────────────────────────────────────────

    private fun saveAll(list: List<AdminMilestone>) {
        prefs.edit().putString(KEY_MILESTONES, gson.toJson(list)).apply()
    }

    companion object {
        private const val KEY_MILESTONES      = "admin_milestone_list"
        private const val KEY_ADMIN_PASSWORD  = "admin_password"
        private const val DEFAULT_PASSWORD    = "admin123"
    }
}
