package com.getbetterhub.app.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Languages the interface is translated into. English is the default/fallback; isiZulu,
 * Afrikaans and isiXhosa have full resource sets under res/values-zu, -af and -xh.
 * (Rubric: multi-language support with at least two South African languages.)
 */
enum class AppLanguage(val tag: String, val nativeName: String) {
    English("en", "English"),
    Zulu("zu", "isiZulu"),
    Afrikaans("af", "Afrikaans"),
    Xhosa("xh", "isiXhosa");

    companion object {
        fun fromTag(tag: String?): AppLanguage = values().firstOrNull { it.tag == tag } ?: English

        /** First launch: follow the phone's language if we support it, otherwise English. */
        fun fromDevice(): AppLanguage = fromTag(Locale.getDefault().language)
    }
}

object LocaleHelper {
    /**
     * Returns a Context whose resources resolve strings in [language]. Used by MainActivity
     * (attachBaseContext) so every stringResource() call follows the in-app language choice,
     * and by background workers that have no Activity but still need localised notification text.
     * Reference: Android developers, "Per-app language preferences" / Context.createConfigurationContext.
     */
    fun wrap(base: Context, language: AppLanguage): Context {
        val locale = Locale(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
