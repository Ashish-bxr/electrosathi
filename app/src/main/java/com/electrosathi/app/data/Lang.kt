package com.electrosathi.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Lang — Batch 16, multilingual support (English / हिन्दी).
 *
 * The mentoring session asked for multilingual support. The honest
 * Phase 1 scope: the app's interface chrome — navigation, drawer,
 * section titles, the Volt screen — switches language instantly,
 * and Volt itself answers in Hindi when Hindi is chosen. Lesson
 * CONTENT (topics, flashcards, books) stays English in this round;
 * translating the full study library is Phase 2 work, and the report
 * says so plainly.
 *
 * How it works: [code] is Compose snapshot state, so every screen
 * reading [tr] recomposes the moment the language changes — no
 * restart. The choice persists in its own prefs file ("settings"),
 * so it survives sign-out (which clears the profile prefs).
 * [tr] takes the English string as its own key: an untranslated
 * string simply stays English, never blank, never a crash.
 */
object Lang {

    private const val STORE = "settings"
    private const val KEY_LANG = "lang"

    var code: String by mutableStateOf("en")
        private set

    val isHindi: Boolean get() = code == "hi"

    fun init(context: Context) {
        code = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "en") ?: "en"
    }

    fun set(context: Context, newCode: String) {
        code = newCode
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, newCode).apply()
    }

    /** The [english] string in the current language. */
    fun tr(english: String): String =
        if (isHindi) HINDI[english] ?: english else english

    private val HINDI: Map<String, String> = mapOf(
        "Home" to "होम",
        "Study" to "पढ़ाई",
        "Exams" to "परीक्षाएँ",
        "Projects Hub" to "प्रोजेक्ट हब",
        "Projects" to "प्रोजेक्ट",
        "Opportunities" to "अवसर",
        "Career" to "करियर",
        "Digital Library" to "डिजिटल लाइब्रेरी",
        "Library" to "लाइब्रेरी",
        "Volt" to "वोल्ट",
        "Live Hackathons" to "लाइव हैकाथॉन",
        "Hackathons" to "हैकाथॉन",
        "Skill Show" to "स्किल शो",
        "Admin Panel" to "एडमिन पैनल",
        "Admin" to "एडमिन",
        "My Pathway" to "मेरा पाथवे",
        "My Progress" to "मेरी प्रगति",
        "Sign in" to "साइन इन",
        "Log out" to "लॉग आउट",
        "Language" to "भाषा",
        "Chat with Volt" to "वोल्ट से बात करें",
        "Ask Volt anything..." to "वोल्ट से कुछ भी पूछें...",
        "Your Focus" to "आपका फ़ोकस"
    )
}
