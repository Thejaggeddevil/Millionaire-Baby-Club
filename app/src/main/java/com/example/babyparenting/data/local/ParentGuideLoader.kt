package com.example.babyparenting.data.local

import android.content.Context
import com.example.babyparenting.data.model.ParentGuide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Reads the two parent-guide CSVs from assets/datasets/
 *
 *   25_60_data_parent.csv  → 2–5 years  (1,211 rows)
 *   5_12_year_data_parent.csv → 5–12 years (3,780 rows)
 *
 * Also loads 0_24_data_parent.csv for completeness.
 */
class ParentGuideLoader(private val context: Context) {

    suspend fun loadAll(): List<ParentGuide> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ParentGuide>()
        result += loadCsv("0_24_data_parent.csv",    ageKey = "age_month", group = "0–2 Years")
        result += loadCsv("25_60_data_parent.csv",   ageKey = "age_month", group = "2–5 Years")
        result += loadCsv("5_12_year_data_parent.csv", ageKey = "age_year", group = "5–12 Years")
        result
    }

    private fun loadCsv(filename: String, ageKey: String, group: String): List<ParentGuide> {
        return try {
            val stream  = context.assets.open("datasets/$filename")
            val reader  = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
            val lines   = reader.readLines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return emptyList()
            val headers = parseCsvLine(lines[0].trimStart('\uFEFF'))
            lines.drop(1).mapIndexedNotNull { idx, line ->
                val values = parseCsvLine(line)
                if (values.size < headers.size) return@mapIndexedNotNull null
                val row = headers.zip(values).toMap()
                ParentGuide(
                    id            = "${group}_${idx}",
                    ageRange      = row.col(ageKey),
                    domain        = row.col("domain"),
                    skillName     = row.col("skill_name"),
                    learningGoal  = row.col("parent_learning_goal"),
                    whyItMatters  = row.col("why_it_matters"),
                    howToTeach    = row.col("how_to_teach"),
                    dos           = parseList(row.col("parent_dos")),
                    donts         = parseList(row.col("parent_donts")),
                    tip           = row.col("parent_tip"),
                    ageGroupLabel = group
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun Map<String, String>.col(vararg keys: String): String {
        for (k in keys) {
            val v = (this as Map<*, *>)[k]
            if (v is String && v.isNotBlank()) return v.trim()
        }
        return ""
    }

    private fun parseList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.trim()
            .removePrefix("[").removeSuffix("]")
            .split(",")
            .map { it.trim().removePrefix("'").removeSuffix("'").removePrefix("\"").removeSuffix("\"").trim() }
            .filter { it.isNotBlank() }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result   = mutableListOf<String>()
        val current  = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && !inQuotes -> inQuotes = true
                ch == '"' && inQuotes  -> {
                    if (i + 1 < line.length && line[i + 1] == '"') { current.append('"'); i++ }
                    else inQuotes = false
                }
                ch == ',' && !inQuotes -> { result.add(current.toString().trim()); current.clear() }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }
}
