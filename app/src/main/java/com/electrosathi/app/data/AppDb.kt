package com.electrosathi.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * AppDb — Batch 14 (Offline Core). The app's own on-device SQLite
 * database. The mentoring-session rule for PS4 is that EVERYTHING is
 * 100% offline — student profile, content, roadmap, quizzes — so the
 * cloud backend is gone and this file is where accounts and
 * admin-published content now live: inside the phone, in SQLite.
 *
 * Schema v1 (Batch 14):
 *  - accounts: local sign-in accounts (email + salted password hash).
 *    The FIRST account created on a phone is that phone's admin
 *    (device owner); every later account is a student account.
 *  - content_lists: the admin panel's published lists, one row per
 *    collection, holding the FULL list as JSON — the same full-list
 *    rule the old Firestore sync used: a stored list replaces the
 *    built-in list for that collection, wholesale.
 *
 * Schema v2 (Batch 15):
 *  - chat_messages: every Volt conversation, stored in SQLite on the
 *    phone as the mentoring session requires — chats survive leaving
 *    the screen and restarting the app, still fully offline.
 *
 * Schema v3 (Batch 16):
 *  - skill_attempts: EVERY Skill Check attempt (not just the latest),
 *    so the improvement report can compare first vs latest in real
 *    time — the data behind My Progress.
 *
 * No new libraries: SQLiteOpenHelper and org.json ship with Android.
 */
class AppDb private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS accounts (" +
                "email TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL DEFAULT '', " +
                "salt TEXT NOT NULL, " +
                "pass_hash TEXT NOT NULL, " +
                "is_admin INTEGER NOT NULL DEFAULT 0, " +
                "created_at INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS content_lists (" +
                "collection TEXT PRIMARY KEY, " +
                "json TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS chat_messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "role TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "ts INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS skill_attempts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ts INTEGER NOT NULL, " +
                "analog INTEGER NOT NULL, " +
                "digital INTEGER NOT NULL, " +
                "signals INTEGER NOT NULL, " +
                "weak TEXT NOT NULL DEFAULT '')"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS chat_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "role TEXT NOT NULL, " +
                    "text TEXT NOT NULL, " +
                    "ts INTEGER NOT NULL)"
            )
        }
        if (oldVersion < 3) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS skill_attempts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ts INTEGER NOT NULL, " +
                    "analog INTEGER NOT NULL, " +
                    "digital INTEGER NOT NULL, " +
                    "signals INTEGER NOT NULL, " +
                    "weak TEXT NOT NULL DEFAULT '')"
            )
        }
    }

    companion object {
        private const val DB_NAME = "electrosathi.db"
        private const val DB_VERSION = 3

        @Volatile
        private var instance: AppDb? = null

        fun get(context: Context): AppDb =
            instance ?: synchronized(this) {
                instance ?: AppDb(context).also { instance = it }
            }
    }
}

/** A local account row, without the password material. */
data class LocalAccount(
    val email: String,
    val name: String,
    val isAdmin: Boolean
)

/**
 * LocalAccounts — sign-in accounts stored in SQLite on the phone.
 * Passwords are never stored in plain text: each account gets a
 * random salt, and only the SHA-256 hash of salt + password is kept.
 */
object LocalAccounts {

    private fun sha256Hex(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun count(context: Context): Int = runCatching {
        val db = AppDb.get(context).readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM accounts", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }.getOrDefault(0)

    fun find(context: Context, email: String): LocalAccount? = runCatching {
        val db = AppDb.get(context).readableDatabase
        db.query(
            "accounts", arrayOf("email", "name", "is_admin"),
            "email = ?", arrayOf(email.trim().lowercase()),
            null, null, null
        ).use { c ->
            if (c.moveToFirst()) {
                LocalAccount(
                    email = c.getString(0),
                    name = c.getString(1),
                    isAdmin = c.getInt(2) == 1
                )
            } else {
                null
            }
        }
    }.getOrNull()

    /** Creates an account; null when the email already has one. The
     *  first account on the phone becomes its admin (device owner). */
    fun create(context: Context, email: String, password: String): LocalAccount? {
        val clean = email.trim().lowercase()
        if (find(context, clean) != null) return null
        val salt = newSalt()
        val isFirst = count(context) == 0
        val values = ContentValues().apply {
            put("email", clean)
            put("name", "")
            put("salt", salt)
            put("pass_hash", sha256Hex("$salt:$password"))
            put("is_admin", if (isFirst) 1 else 0)
            put("created_at", System.currentTimeMillis())
        }
        val rowId = runCatching {
            AppDb.get(context).writableDatabase.insertOrThrow("accounts", null, values)
        }.getOrDefault(-1L)
        if (rowId < 0) return null
        return LocalAccount(email = clean, name = "", isAdmin = isFirst)
    }

    /** The account when email + password match, else null. */
    fun verify(context: Context, email: String, password: String): LocalAccount? =
        runCatching {
            val clean = email.trim().lowercase()
            val db = AppDb.get(context).readableDatabase
            db.query(
                "accounts", arrayOf("email", "name", "is_admin", "salt", "pass_hash"),
                "email = ?", arrayOf(clean),
                null, null, null
            ).use { c ->
                if (!c.moveToFirst()) {
                    null
                } else {
                    val salt = c.getString(3)
                    val storedHash = c.getString(4)
                    if (sha256Hex("$salt:$password") == storedHash) {
                        LocalAccount(
                            email = c.getString(0),
                            name = c.getString(1),
                            isAdmin = c.getInt(2) == 1
                        )
                    } else {
                        null
                    }
                }
            }
        }.getOrNull()
}

/**
 * LocalContent — the admin panel's published lists, stored in SQLite
 * as JSON. A stored list REPLACES the built-in list for its
 * collection (full-list rule); no stored list means the app shows
 * its built-in content. Field maps use the same keys the admin forms
 * have always produced (name/title/org/…), so forms didn't change.
 */
object LocalContent {

    fun loadList(context: Context, collection: String): List<Map<String, Any>>? =
        runCatching {
            val db = AppDb.get(context).readableDatabase
            val json = db.query(
                "content_lists", arrayOf("json"),
                "collection = ?", arrayOf(collection),
                null, null, null
            ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
                ?: return null
            val array = JSONArray(json)
            val out = ArrayList<Map<String, Any>>(array.length())
            for (i in 0 until array.length()) {
                out.add(jsonToMap(array.getJSONObject(i)))
            }
            out
        }.getOrNull()

    fun saveList(context: Context, collection: String, list: List<Map<String, Any>>) {
        val array = JSONArray()
        list.forEach { array.put(mapToJson(it)) }
        val values = ContentValues().apply {
            put("collection", collection)
            put("json", array.toString())
        }
        runCatching {
            AppDb.get(context).writableDatabase
                .insertWithOnConflict(
                    "content_lists", null, values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
        }
    }

    private fun mapToJson(map: Map<String, Any>): JSONObject {
        val obj = JSONObject()
        for ((key, value) in map) {
            when (value) {
                is String -> obj.put(key, value)
                is Number -> obj.put(key, value)
                is Boolean -> obj.put(key, value)
                is List<*> -> {
                    val arr = JSONArray()
                    value.forEach { arr.put(it.toString()) }
                    obj.put(key, arr)
                }
                else -> obj.put(key, value.toString())
            }
        }
        return obj
    }

    private fun jsonToMap(obj: JSONObject): Map<String, Any> {
        val out = LinkedHashMap<String, Any>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            when (val value = obj.get(key)) {
                is JSONArray -> {
                    val list = ArrayList<String>(value.length())
                    for (i in 0 until value.length()) list.add(value.optString(i))
                    out[key] = list
                }
                is Number, is String, is Boolean -> out[key] = value
                else -> out[key] = value.toString()
            }
        }
        return out
    }
}

/**
 * ChatStore — Batch 15. Volt's conversation history in SQLite
 * (chat_messages). Every question and answer is saved the moment it
 * appears; opening the Volt screen reloads the whole conversation.
 * Roles: "user" for the student's bubbles, "volt" for Volt's.
 */
object ChatStore {

    fun add(context: Context, role: String, text: String) {
        if (text.isBlank()) return
        runCatching {
            val values = ContentValues().apply {
                put("role", role)
                put("text", text)
                put("ts", System.currentTimeMillis())
            }
            AppDb.get(context).writableDatabase.insert("chat_messages", null, values)
        }
    }

    /** The whole conversation, oldest first, as (isUser, text) pairs —
     *  exactly the shape the Volt screen's message list uses. */
    fun loadAll(context: Context): List<Pair<Boolean, String>> = runCatching {
        val db = AppDb.get(context).readableDatabase
        db.query(
            "chat_messages", arrayOf("role", "text"),
            null, null, null, null, "id ASC"
        ).use { c ->
            val out = ArrayList<Pair<Boolean, String>>()
            while (c.moveToNext()) {
                out.add((c.getString(0) == "user") to c.getString(1))
            }
            out
        }
    }.getOrDefault(emptyList())

    fun count(context: Context): Int = runCatching {
        val db = AppDb.get(context).readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM chat_messages", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }.getOrDefault(0)
}

/** One saved Skill Check attempt (Batch 16). */
data class SkillAttempt(
    val takenAt: Long,
    val analog: Int,
    val digital: Int,
    val signals: Int,
    val weakTopics: List<String>
) {
    val total: Int get() = analog + digital + signals

    /** Per-subject score out of 4, keyed by Repository subject id. */
    fun scoreFor(subjectId: String): Int = when (subjectId) {
        "analog-circuits" -> analog
        "digital-electronics" -> digital
        else -> signals
    }
}

/**
 * SkillAttempts — Batch 16. The history behind the real-time
 * improvement report: every finished Skill Check appends one row
 * here (UserProfile.saveSkillResults does it), so My Progress can
 * compare the first attempt with the latest the moment a retake
 * finishes — no server, no waiting, everything on the phone.
 */
object SkillAttempts {

    fun add(
        context: Context,
        analog: Int,
        digital: Int,
        signals: Int,
        weakTopics: List<String>
    ) {
        runCatching {
            val values = ContentValues().apply {
                put("ts", System.currentTimeMillis())
                put("analog", analog)
                put("digital", digital)
                put("signals", signals)
                put("weak", weakTopics.joinToString("\n"))
            }
            AppDb.get(context).writableDatabase.insert("skill_attempts", null, values)
        }
    }

    /** All attempts, oldest first. */
    fun all(context: Context): List<SkillAttempt> = runCatching {
        val db = AppDb.get(context).readableDatabase
        db.query(
            "skill_attempts",
            arrayOf("ts", "analog", "digital", "signals", "weak"),
            null, null, null, null, "id ASC"
        ).use { c ->
            val out = ArrayList<SkillAttempt>()
            while (c.moveToNext()) {
                out.add(
                    SkillAttempt(
                        takenAt = c.getLong(0),
                        analog = c.getInt(1),
                        digital = c.getInt(2),
                        signals = c.getInt(3),
                        weakTopics = (c.getString(4) ?: "")
                            .split("\n").filter { it.isNotBlank() }
                    )
                )
            }
            out
        }
    }.getOrDefault(emptyList())
}
