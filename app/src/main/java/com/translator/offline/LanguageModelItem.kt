package com.translator.offline

import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

data class LanguageModelItem(
    val code: String,
    val name: String,
    var isDownloaded: Boolean = false,
    var isDownloading: Boolean = false
)

object SupportedLanguages {
    val languages = listOf(
        LanguageModelItem(TranslateLanguage.HEBREW, "עברית (Hebrew)"),
        LanguageModelItem(TranslateLanguage.ENGLISH, "אנגלית (English)"),
        LanguageModelItem(TranslateLanguage.ARABIC, "ערבית (Arabic)"),
        LanguageModelItem(TranslateLanguage.RUSSIAN, "רוסית (Russian)"),
        LanguageModelItem(TranslateLanguage.FRENCH, "צרפתית (French)"),
        LanguageModelItem(TranslateLanguage.GERMAN, "גרמנית (German)"),
        LanguageModelItem(TranslateLanguage.SPANISH, "ספרדית (Spanish)"),
        LanguageModelItem(TranslateLanguage.ITALIAN, "איטלקית (Italian)"),
        LanguageModelItem(TranslateLanguage.JAPANESE, "יפנית (Japanese)"),
        LanguageModelItem(TranslateLanguage.CHINESE, "סינית (Chinese)")
    )
}
