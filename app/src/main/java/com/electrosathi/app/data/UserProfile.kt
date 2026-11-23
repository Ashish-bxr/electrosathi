package com.electrosathi.app.data

import android.content.Context
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Student accounts & personalisation.
 *
 * Batch 14 (Offline Core): accounts are now 100% on-device, as the
 * PS4 mentoring session requires — no cloud backend anywhere.
 * - Identity lives in the app's SQLite database (see [LocalAccounts]):
 *   email + salted password hash, created and checked on the phone.
 *   Guests get no account — just a local flag.
 * - The session is simply the signed-in email, kept in the
 *   SharedPreferences cache ("profile").
 * - The profile itself (name / who / goal) has always lived in that
 *   same on-device cache, so Home can greet instantly; nothing about
 *   the greeting, onboarding or Skill Check storage needed a server.
 * - Admin power is a flag on the local account: the FIRST account
 *   created on a phone is that phone's admin (device owner) — the
 *   same account the Admin Panel checks before opening.
 */
object UserProfile {

    private const val STORE = "profile"
    private const val KEY_NAME = "name"
    private const val KEY_WHO = "who"
    private const val KEY_GOAL = "goal"
    private const val KEY_LEVEL = "level"
    private const val KEY_GUEST = "guest"
    private const val KEY_ONBOARDED = "onboarded"
    private const val KEY_SESSION = "sessionEmail"
    private const val KEY_ADMIN = "isAdmin"
    private const val KEY_ADMIN_UID = "adminUid"
    private const val KEY_SC_DONE = "scDone"
    private const val KEY_SC_SKIPPED = "scSkipped"
    private const val KEY_SC_ANALOG = "scAnalog"
    private const val KEY_SC_DIGITAL = "scDigital"
    private const val KEY_SC_SIGNALS = "scSignals"
    private const val KEY_SC_WEAK = "scWeak"
    private const val KEY_SC_AT = "scAt"

    private fun prefs(context: Context) =
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE)

    // ------------------------------------------------------------------
    // Local cache — onboarding answers + greeting name + session flags.
    // ------------------------------------------------------------------
    fun saveOnboardingAnswers(
        context: Context,
        name: String,
        who: String,
        goal: String,
        level: String
    ) {
        prefs(context).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_WHO, who)
            .putString(KEY_GOAL, goal)
            .putString(KEY_LEVEL, level)
            .putBoolean(KEY_ONBOARDED, true)
            .apply()
    }

    fun onboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDED, false)

    fun cachedName(context: Context): String =
        prefs(context).getString(KEY_NAME, "") ?: ""

    private fun cachedWho(context: Context): String =
        prefs(context).getString(KEY_WHO, "") ?: ""

    private fun cachedGoal(context: Context): String =
        prefs(context).getString(KEY_GOAL, "") ?: ""

    private fun cacheName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name).apply()
    }

    fun isGuest(context: Context): Boolean =
        prefs(context).getBoolean(KEY_GUEST, false)

    fun setGuest(context: Context, guest: Boolean) {
        prefs(context).edit().putBoolean(KEY_GUEST, guest).apply()
    }

    // ------------------------------------------------------------------
    // Session — the signed-in account's email, stored on-device.
    // ------------------------------------------------------------------
    fun setSession(context: Context, email: String) {
        prefs(context).edit().putString(KEY_SESSION, email.trim().lowercase()).apply()
    }

    fun clearSession(context: Context) {
        prefs(context).edit().remove(KEY_SESSION).apply()
    }

    fun sessionEmail(context: Context): String? =
        prefs(context).getString(KEY_SESSION, null)?.takeIf { it.isNotBlank() }

    fun isSignedIn(context: Context): Boolean = sessionEmail(context) != null

    /** The signed-in local account, when a session exists. */
    fun currentAccount(context: Context): LocalAccount? =
        sessionEmail(context)?.let { LocalAccounts.find(context, it) }

    /**
     * Admin verification for the publishing panel: the signed-in local
     * account must carry the admin flag. On a phone, the first account
     * created is the admin (device owner) — see [LocalAccounts].
     * The read is instant and offline, so no cache is needed anymore.
     */
    fun isAdmin(context: Context, onDone: (Boolean) -> Unit) {
        onDone(currentAccount(context)?.isAdmin == true)
    }

    /** Kept for the panel's sign-out path; the old cloud-verdict cache
     *  keys are simply cleared. */
    fun clearAdminCache(context: Context) {
        prefs(context).edit().remove(KEY_ADMIN).remove(KEY_ADMIN_UID).apply()
    }

    /** Signs out and clears the whole local profile cache. */
    fun signOut(context: Context) {
        prefs(context).edit().clear().apply()
    }

    // ------------------------------------------------------------------
    // Sign-in methods — all local. Callbacks mirror the panel style;
    // the message is already user-friendly.
    // ------------------------------------------------------------------
    fun signInEmail(
        context: Context,
        email: String,
        password: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        val clean = email.trim()
        if (LocalAccounts.find(context, clean) == null) {
            onDone(false, "No account found for this email on this phone — create one first.")
            return
        }
        val account = LocalAccounts.verify(context, clean, password)
        if (account == null) {
            onDone(false, "Wrong email or password — please check and try again.")
        } else {
            setSession(context, account.email)
            onDone(true, null)
        }
    }

    fun signUpEmail(
        context: Context,
        email: String,
        password: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        val clean = email.trim()
        if (!clean.contains("@") || !clean.contains(".")) {
            onDone(false, "That email doesn't look right — please check it.")
            return
        }
        if (password.length < 6) {
            onDone(false, "Password is too weak — please use at least 6 characters.")
            return
        }
        if (LocalAccounts.find(context, clean) != null) {
            onDone(false, "This email already has an account on this phone — try signing in instead.")
            return
        }
        val account = LocalAccounts.create(context, clean, password)
        if (account == null) {
            onDone(false, "Couldn't create the account — please try again.")
        } else {
            setSession(context, account.email)
            onDone(true, null)
        }
    }

    // ------------------------------------------------------------------
    // Profile resolution — fully local.
    // ------------------------------------------------------------------

    /**
     * Resolves the display name for the signed-in user: the onboarding
     * name wins; otherwise the account's stored name; otherwise the
     * part of the email before "@". Always calls back immediately —
     * there is no cloud to wait for.
     */
    fun upsertProfile(context: Context, onDone: (String) -> Unit) {
        val account = currentAccount(context)
        val resolved = cachedName(context)
            .ifBlank { account?.name ?: "" }
            .ifBlank {
                account?.email?.substringBefore("@")
                    ?.replaceFirstChar { it.uppercase() } ?: ""
            }
        if (resolved.isNotBlank()) cacheName(context, resolved)
        onDone(resolved)
    }

    /**
     * App-start refresh: the profile lives on this phone, so the
     * cached name is already the freshest one there is.
     */
    fun refreshCachedProfile(context: Context, onName: (String) -> Unit) {
        onName(cachedName(context))
    }

    // ------------------------------------------------------------------
    // Skill Check results (Batch 10). Stored in the same "profile"
    // prefs — on-device only, like everything else in the app.
    // ------------------------------------------------------------------

    data class SkillResults(
        val done: Boolean,
        val skipped: Boolean,
        val analog: Int,
        val digital: Int,
        val signals: Int,
        val weakTopics: List<String>,
        val takenAt: Long
    ) {
        /** Per-subject score out of 4, keyed by Repository subject id. */
        fun scoreFor(subjectId: String): Int = when (subjectId) {
            "analog-circuits" -> analog
            "digital-electronics" -> digital
            else -> signals
        }
    }

    fun skillResults(context: Context): SkillResults {
        val p = prefs(context)
        return SkillResults(
            done = p.getBoolean(KEY_SC_DONE, false),
            skipped = p.getBoolean(KEY_SC_SKIPPED, false),
            analog = p.getInt(KEY_SC_ANALOG, 0),
            digital = p.getInt(KEY_SC_DIGITAL, 0),
            signals = p.getInt(KEY_SC_SIGNALS, 0),
            weakTopics = (p.getString(KEY_SC_WEAK, "") ?: "")
                .split("\n").filter { it.isNotBlank() },
            takenAt = p.getLong(KEY_SC_AT, 0L)
        )
    }

    /** True when the first-run Skill Check should still be offered:
     *  onboarding finished, quiz neither taken nor skipped. */
    fun needsSkillCheck(context: Context): Boolean {
        if (!onboardingDone(context)) return false
        val r = skillResults(context)
        return !r.done && !r.skipped
    }

    /** The stored onboarding goal ("" when never answered). */
    fun goal(context: Context): String = cachedGoal(context)

    /** The stored onboarding "who" answer ("" when never answered).
     *  Public since Batch 17 — the Profile screen shows it. */
    fun who(context: Context): String = cachedWho(context)

    /** The stored onboarding level answer ("" when never answered). */
    fun level(context: Context): String =
        prefs(context).getString(KEY_LEVEL, "") ?: ""

    /** "12 Nov 2026" for the last Skill Check, "" when never taken. */
    fun skillCheckWhenText(context: Context): String {
        val at = skillResults(context).takenAt
        if (at <= 0L) return ""
        val date = Instant.ofEpochMilli(at)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    }

    /** Saves a finished Skill Check (first take or retake — the latest
     *  result replaces the headline scores; Batch 16 also appends the
     *  attempt to the on-device history behind the improvement
     *  report, so progress over time is never lost). */
    fun saveSkillResults(
        context: Context,
        analog: Int,
        digital: Int,
        signals: Int,
        weakTopics: List<String>
    ) {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putBoolean(KEY_SC_DONE, true)
            .putBoolean(KEY_SC_SKIPPED, false)
            .putInt(KEY_SC_ANALOG, analog)
            .putInt(KEY_SC_DIGITAL, digital)
            .putInt(KEY_SC_SIGNALS, signals)
            .putString(KEY_SC_WEAK, weakTopics.joinToString("\n"))
            .putLong(KEY_SC_AT, now)
            .apply()
        // Batch 16: every attempt is also appended to the SQLite
        // history behind the real-time improvement report.
        SkillAttempts.add(context, analog, digital, signals, weakTopics)
    }

    /** "Skip for now": remembered, so the first-run gate doesn't nag. */
    fun markSkillCheckSkipped(context: Context) {
        prefs(context).edit().putBoolean(KEY_SC_SKIPPED, true).apply()
    }
}
