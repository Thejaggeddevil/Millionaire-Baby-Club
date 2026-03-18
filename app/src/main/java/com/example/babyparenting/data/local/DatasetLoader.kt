package com.example.babyparenting.data.local

import android.content.Context
import com.example.babyparenting.data.model.AgeGroup
import com.example.babyparenting.data.model.DatasetSource
import com.example.babyparenting.data.model.Milestone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Reads all 15 CSV files from assets/datasets/ at runtime.
 * Nothing hardcoded — every milestone comes from the CSVs.
 *
 * ── SETUP ────────────────────────────────────────────────────────────────────
 * Copy all 15 CSV files into:  app/src/main/assets/datasets/
 *
 * Expected filenames:
 *   0_24_month_data.csv          0_24_data_parent.csv
 *   24_60_month_data.csv         25_60_data_parent.csv
 *   2_5_academics_data.csv       5_12_year_data.csv
 *   5_12_year_data_parent.csv    language_5_12_data.csv
 *   maths_5_12_data.csv          science_5_12_data.csv
 *   social_5_12_data.csv         civics_evs_5_12_data.csv
 *   cs_5_12_data.csv             foreign_5_12_data.csv
 *   good_bad_touch_data.csv
 * ─────────────────────────────────────────────────────────────────────────────
 */
class DatasetLoader(private val context: Context) {

    suspend fun loadInitialMilestones(): List<Milestone> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Milestone>()

        // 🔥 ONLY LOAD FIRST 2 FILES (for testing)
        list += load0to24Child()
        list += load0to24Parent()

        android.util.Log.d("CSV_DEBUG", "Loaded initial: ${list.size}")

        list
    }

    fun getAgeGroups(): List<AgeGroup> = listOf(
        AgeGroup(1,  "0 – 3 Months",   "Sensory foundation, feeding & safe sleep",        0,   3,   0xFFFF8B94),
        AgeGroup(2,  "3 – 6 Months",   "Tummy time, head control & social smiles",         3,   6,   0xFFFFB347),
        AgeGroup(3,  "6 – 9 Months",   "Sitting, solid foods & babbling",                 6,   9,   0xFFFFC75F),
        AgeGroup(4,  "9 – 12 Months",  "Crawling, standing & first words",                9,   12,  0xFF98D8C8),
        AgeGroup(5,  "1 – 1.5 Years",  "Walking, vocabulary burst & play skills",         12,  18,  0xFFB5EAD7),
        AgeGroup(6,  "1.5 – 2 Years",  "Running, emotional growth & self-feeding",        18,  24,  0xFF7C83FD),
        AgeGroup(7,  "2 – 3 Years",    "Language, potty training & pre-academics",        24,  36,  0xFFADD8E6),
        AgeGroup(8,  "3 – 4 Years",    "Pre-K: drawing, safety & creativity",             36,  48,  0xFFD4A5F5),
        AgeGroup(9,  "4 – 5 Years",    "Kindergarten: reading, writing & body safety",    48,  60,  0xFFFDDB92),
        AgeGroup(10, "5 – 7 Years",    "School: core subjects, language & safety",        60,  84,  0xFF66BB6A),
        AgeGroup(11, "7 – 9 Years",    "Advanced academics, STEM & social skills",        84,  108, 0xFF42A5F5),
        AgeGroup(12, "9 – 11 Years",   "Critical thinking, CS & foreign language",        108, 132, 0xFFFF6B6B),
        AgeGroup(13, "11 – 12 Years",  "Pre-teen independence, coding & digital ethics",  132, 144, 0xFF26C6DA)
    )

    // ── CSV helpers ───────────────────────────────────────────────────────────

    private fun openCsv(filename: String): List<Map<String, String>> {
        return try {
            val stream  = context.assets.open("datasets/$filename")
            val reader  = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
            val lines   = reader.readLines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return emptyList()
            val headers = parseCsvLine(lines[0].trimStart('\uFEFF'))
            lines.drop(1).mapNotNull { line ->
                val values = parseCsvLine(line)
                if (values.size < headers.size) return@mapNotNull null
                headers.zip(values).toMap()
            }
        } catch (e: Exception) {
            android.util.Log.e("CSV_ERROR", "Error loading $filename: ${e.message}")
            emptyList()
        }
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

    /**
     * FIX: renamed to col() instead of get() to avoid shadowing Map.get()
     * which caused 175 "type mismatch String? vs String" compile errors.
     */
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
            .replace("\"", "")
            .split(",")
            .map { it.trim().removePrefix("'").removeSuffix("'").trim() }
            .filter { it.isNotBlank() }
    }

    private fun ageGroupForMonth(months: Int): Int = when (months) {
        in 0..2    -> 1
        in 3..5    -> 2
        in 6..8    -> 3
        in 9..11   -> 4
        in 12..17  -> 5
        in 18..23  -> 6
        in 24..35  -> 7
        in 36..47  -> 8
        in 48..59  -> 9
        in 60..83  -> 10
        in 84..107 -> 11
        in 108..131-> 12
        else       -> 13
    }

    private fun parseAgeMonth(raw: String): Int {
        val s = raw.trim()
        return when {
            s.toIntOrNull() != null -> s.toInt()
            s.contains("-")        -> s.split("-")[0].trim().toIntOrNull() ?: 0
            s.contains("–")        -> s.split("–")[0].trim().toIntOrNull() ?: 0
            else                   -> 0
        }
    }

    private fun parseAgeYearToMonths(raw: String): Int {
        val s = raw.trim()
        return when {
            s.contains("-")            -> (s.split("-")[0].trim().toDoubleOrNull() ?: 0.0).times(12).toInt()
            s.contains("–")            -> (s.split("–")[0].trim().toDoubleOrNull() ?: 0.0).times(12).toInt()
            s.toDoubleOrNull() != null -> (s.toDouble() * 12).toInt()
            else                       -> 60
        }
    }

    private fun ageRawToMonths(raw: String): Int = when {
        raw.contains("11") -> 132
        raw.contains("9")  -> 108
        raw.contains("8")  -> 96
        raw.contains("7")  -> 84
        raw.contains("5")  -> 60
        else               -> 60
    }

    private fun buildQuery(domain: String, skill: String, age: String) =
        "$domain $skill $age".trim()

    // ── 0_24_month_data.csv ───────────────────────────────────────────────────

    private fun load0to24Child(): List<Milestone> =
        openCsv("0_24_month_data.csv").take(200) .mapIndexed { idx, row ->
            val age    = parseAgeMonth(row.col("age_month"))
            val domain = row.col("domain")
            val skill  = row.col("skill")
            Milestone(
                id          = "c0_24_$idx",
                title       = skill.take(50),
                subtitle    = row.col("development_goal").take(70),
                domain      = domain,
                ageMonths   = age,
                ageRange    = "$age mo",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.CHILD_0_24,
                apiQuery    = buildQuery(domain, skill, "$age months"),
                iconEmoji   = emoji0to24(domain),
                accentColor = DatasetSource.CHILD_0_24.colorHex
            )
        }

    // ── 0_24_data_parent.csv ──────────────────────────────────────────────────

    private fun load0to24Parent(): List<Milestone> =
        openCsv("0_24_data_parent.csv").take(200).mapIndexed { idx, row ->
            val age    = parseAgeMonth(row.col("age_month"))
            val domain = row.col("domain")
            val skill  = row.col("skill_name")
            Milestone(
                id          = "p0_24_$idx",
                title       = skill.take(50),
                subtitle    = row.col("parent_learning_goal").take(70),
                domain      = domain,
                ageMonths   = age,
                ageRange    = "$age mo",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.PARENT_0_24,
                apiQuery    = buildQuery(domain, skill, "$age months parent"),
                iconEmoji   = emojiParent(domain),
                accentColor = DatasetSource.PARENT_0_24.colorHex
            )
        }

    // ── 24_60_month_data.csv ──────────────────────────────────────────────────

    private fun load24to60Child(): List<Milestone> =
        openCsv("24_60_month_data.csv").take(200).mapIndexed { idx, row ->
            val ageRaw = row.col("age_month_range")
            val age    = parseAgeMonth(ageRaw)
            val domain = row.col("domain")
            val act    = row.col("activity")
            Milestone(
                id          = "c24_60_$idx",
                title       = act.take(50),
                subtitle    = row.col("goal").take(70),
                domain      = domain,
                ageMonths   = age,
                ageRange    = "$ageRaw mo",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.CHILD_24_60,
                apiQuery    = buildQuery(domain, act, "$ageRaw months"),
                iconEmoji   = emoji24to60(domain),
                accentColor = DatasetSource.CHILD_24_60.colorHex
            )
        }

    // ── 25_60_data_parent.csv ─────────────────────────────────────────────────

    private fun load24to60Parent(): List<Milestone> =
        openCsv("25_60_data_parent.csv").take(200).mapIndexed { idx, row ->
            val ageRaw = row.col("age_month")
            val age    = parseAgeMonth(ageRaw)
            val domain = row.col("domain")
            val skill  = row.col("skill_name")
            Milestone(
                id          = "p24_60_$idx",
                title       = skill.take(50),
                subtitle    = row.col("parent_learning_goal").take(70),
                domain      = domain,
                ageMonths   = age,
                ageRange    = "$ageRaw mo",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.PARENT_24_60,
                apiQuery    = buildQuery(domain, skill, "$ageRaw months parent"),
                iconEmoji   = emojiParent(domain),
                accentColor = DatasetSource.PARENT_24_60.colorHex
            )
        }

    // ── 2_5_academics_data.csv ────────────────────────────────────────────────

    private fun load2to5Academics(): List<Milestone> =
        openCsv("2_5_academics_data.csv").take(200).mapIndexed { idx, row ->
            val ageRaw = row.col("age_range")
            val age    = parseAgeYearToMonths(ageRaw)
            val domain = row.col("domain")
            val act    = row.col("activity")
            Milestone(
                id          = "acad_$idx",
                title       = act.take(50),
                subtitle    = row.col("goal").take(70),
                domain      = domain,
                ageMonths   = age,
                ageRange    = "$ageRaw yr",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.PRE_ACADEMICS,
                apiQuery    = buildQuery(domain, act, "$ageRaw years"),
                iconEmoji   = emojiAcademics(domain),
                accentColor = DatasetSource.PRE_ACADEMICS.colorHex
            )
        }

    // ── 5_12_year_data.csv ────────────────────────────────────────────────────

    private fun load5to12Child(): List<Milestone> =
        openCsv("5_12_year_data.csv").take(200).mapIndexed { idx, row ->
            val ageRaw = row.col("age_range")
            val age    = parseAgeYearToMonths(ageRaw)
            val domain = row.col("domain")
            val act    = row.col("activity")
            Milestone(
                id          = "c5_12_$idx",
                title       = act.take(50),
                subtitle    = row.col("goal").take(70),
                domain      = domain,
                ageMonths   = age,
                ageRange    = "$ageRaw yr",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.CHILD_5_12,
                apiQuery    = buildQuery(domain, act, "$ageRaw years"),
                iconEmoji   = emoji5to12(domain),
                accentColor = DatasetSource.CHILD_5_12.colorHex
            )
        }

    // ── 5_12_year_data_parent.csv ─────────────────────────────────────────────

    private fun load5to12Parent(): List<Milestone> =
        openCsv("5_12_year_data_parent.csv").take(200).mapIndexed { idx, row ->
            val ageRaw = row.col("age_year")
            val age    = parseAgeYearToMonths(ageRaw)
            val domain = row.col("domain")
            val skill  = row.col("skill_name")
            Milestone(
                id          = "p5_12_$idx",
                title       = skill.take(50),
                subtitle    = row.col("parent_learning_goal").take(70),
                domain      = domain,
                ageMonths   = age,
                ageRange    = "$ageRaw yr",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.PARENT_5_12,
                apiQuery    = buildQuery(domain, skill, "$ageRaw years parent"),
                iconEmoji   = emojiParent(domain),
                accentColor = DatasetSource.PARENT_5_12.colorHex
            )
        }

    // ── language_5_12_data.csv ────────────────────────────────────────────────

    private fun loadLanguage(): List<Milestone> =
        openCsv("language_5_12_data.csv").take(200).mapIndexed { idx, row ->
            val ageRaw   = row.col("age")
            val age      = (ageRaw.toDoubleOrNull() ?: 5.0).times(12).toInt()
            val subdomain = row.col("subdomain")
            val skillName = row.col("skill_name")
            Milestone(
                id          = "lang_$idx",
                title       = skillName.take(50),
                subtitle    = subdomain.take(70),
                domain      = "Language & Communication",
                ageMonths   = age,
                ageRange    = "$ageRaw yr",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.LANGUAGE,
                apiQuery    = buildQuery("Language Communication", skillName, "$ageRaw years"),
                iconEmoji   = "🗣️",
                accentColor = DatasetSource.LANGUAGE.colorHex
            )
        }

    // ── Subject datasets ──────────────────────────────────────────────────────

    private fun loadMaths()   = loadSubject("maths_5_12_data.csv",      DatasetSource.MATHEMATICS,      "maths",   "🔢")
    private fun loadScience() = loadSubject("science_5_12_data.csv",    DatasetSource.SCIENCE,          "science", "🔬")
    private fun loadSocial()  = loadSubject("social_5_12_data.csv",     DatasetSource.SOCIAL_STUDIES,   "social",  "🏘️")
    private fun loadCivics()  = loadSubject("civics_evs_5_12_data.csv", DatasetSource.CIVICS,           "civics",  "🏛️")
    private fun loadCS()      = loadSubject("cs_5_12_data.csv",         DatasetSource.COMPUTER_SCIENCE, "cs",      "💻")

    private fun loadSubject(file: String, source: DatasetSource, prefix: String, emoji: String): List<Milestone> =
        openCsv(file).mapIndexed { idx, row ->
            val ageRaw  = row.col("age_group")
            val age     = ageRawToMonths(ageRaw)
            val subject = row.col("subject")
            val topic   = row.col("topic")
            Milestone(
                id          = "${prefix}_$idx",
                title       = topic.take(50).ifBlank { subject.take(50) },
                subtitle    = row.col("input").take(70).ifBlank { topic.take(70) },
                domain      = subject,
                ageMonths   = age,
                ageRange    = "$ageRaw yr",
                ageGroupId  = ageGroupForMonth(age),
                source      = source,
                apiQuery    = buildQuery(subject, topic, "$ageRaw years"),
                iconEmoji   = emoji,
                accentColor = source.colorHex
            )
        }

    // ── foreign_5_12_data.csv ─────────────────────────────────────────────────

    private fun loadForeign(): List<Milestone> =
        openCsv("foreign_5_12_data.csv").mapIndexed { idx, row ->
            val ageRaw  = row.col("age_group")
            val age     = ageRawToMonths(ageRaw)
            val lang    = row.col("language")
            val topic   = row.col("topic")
            Milestone(
                id          = "foreign_$idx",
                title       = "$lang: ${topic.take(35)}",
                subtitle    = row.col("input").take(70),
                domain      = "Foreign Language",
                ageMonths   = age,
                ageRange    = "$ageRaw yr",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.FOREIGN_LANGUAGE,
                apiQuery    = "Foreign language $lang $topic $ageRaw years child learning",
                iconEmoji   = emojiLang(lang),
                accentColor = DatasetSource.FOREIGN_LANGUAGE.colorHex
            )
        }

    // ── good_bad_touch_data.csv ───────────────────────────────────────────────

    private fun loadSafety(): List<Milestone> =
        openCsv("good_bad_touch_data.csv").mapIndexed { idx, row ->
            val ageRaw = row.col("age_group")
            val age    = when {
                ageRaw.contains("4") -> 48
                ageRaw.contains("6") -> 72
                ageRaw.contains("9") -> 108
                else                 -> 48
            }
            val env   = row.col("environment")
            val topic = row.col("topic")
            Milestone(
                id          = "safety_$idx",
                title       = topic.take(50),
                subtitle    = row.col("learning_goal").take(70),
                domain      = "Safety",
                ageMonths   = age,
                ageRange    = "$ageRaw yr",
                ageGroupId  = ageGroupForMonth(age),
                source      = DatasetSource.SAFETY,
                apiQuery    = "child safety $topic ${row.col("scenario")} $env $ageRaw years",
                iconEmoji   = emojiSafety(env),
                accentColor = DatasetSource.SAFETY.colorHex
            )
        }

    // ── Emoji helpers ─────────────────────────────────────────────────────────

    private fun emoji0to24(d: String) = when {
        d.contains("motor")      -> "💪"
        d.contains("language")   -> "💬"
        d.contains("sensory")    -> "👀"
        d.contains("emotional")  -> "😊"
        d.contains("montessori") -> "🎨"
        d.contains("social")     -> "🤝"
        d.contains("sound")      -> "🔊"
        else                     -> "🌱"
    }

    private fun emojiParent(d: String) = when {
        d.contains("feed")         -> "🍼"
        d.contains("sleep")        -> "🌙"
        d.contains("hygiene")      -> "🛁"
        d.contains("safety")       -> "🛡️"
        d.contains("emotional")    -> "❤️"
        d.contains("development")  -> "🌱"
        d.contains("teamwork")     -> "🤝"
        d.contains("behavior")     -> "⭐"
        d.contains("independence") -> "🦅"
        d.contains("cognitive")    -> "🧠"
        d.contains("language")     -> "🗣️"
        d.contains("academic")     -> "📚"
        d.contains("social")       -> "👥"
        d.contains("motor")        -> "🏃"
        else                       -> "👨‍👩‍👧"
    }

    private fun emoji24to60(d: String) = when {
        d.contains("cognitive")      -> "🧠"
        d.contains("creativity")     -> "🎨"
        d.contains("daily life")     -> "🏠"
        d.contains("early learning") -> "📖"
        d.contains("emotional")      -> "❤️"
        d.contains("language")       -> "💬"
        d.contains("social")         -> "🤝"
        d.contains("nature")         -> "🌿"
        d.contains("motor")          -> "🏃"
        else                         -> "⭐"
    }

    private fun emojiAcademics(d: String) = when {
        d.contains("math")     -> "🔢"
        d.contains("literacy") -> "🔤"
        d.contains("reading")  -> "📖"
        d.contains("writing")  -> "✏️"
        d.contains("art")      -> "🎨"
        d.contains("music")    -> "🎵"
        d.contains("science")  -> "🔬"
        d.contains("social")   -> "🏘️"
        d.contains("physical") -> "⚽"
        else                   -> "📚"
    }

    private fun emoji5to12(d: String) = when {
        d.contains("cognitive")  -> "🧠"
        d.contains("creativity") -> "🎨"
        d.contains("daily")      -> "🏠"
        d.contains("language")   -> "🗣️"
        d.contains("nature")     -> "🌿"
        d.contains("physical")   -> "🏃"
        d.contains("social")     -> "🤝"
        else                     -> "⭐"
    }

    private fun emojiLang(lang: String) = when (lang.lowercase()) {
        "spanish" -> "🇪🇸"; "french"  -> "🇫🇷"; "german"  -> "🇩🇪"
        "arabic"  -> "🇦🇪"; "italian" -> "🇮🇹"; else      -> "🌐"
    }

    private fun emojiSafety(env: String) = when (env.lowercase()) {
        "home"    -> "🏠"; "school"  -> "🏫"; "outdoor" -> "🌳"
        "online"  -> "💻"; "public"  -> "🏙️"; else      -> "🛡️"
    }
}