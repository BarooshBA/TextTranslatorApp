package com.translator.offline

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.translator.offline.databinding.ActivityProcessTextBinding

class ProcessTextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProcessTextBinding
    private var selectedText: String = ""
    private var isReadOnly: Boolean = true
    private var currentTranslator: Translator? = null

    // Supported target languages for dropdown
    private val langOptions = SupportedLanguages.languages
    private var selectedSourceLangCode: String = "AUTO"
    private var selectedTargetLangCode: String = TranslateLanguage.HEBREW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Read intent data from android.intent.action.PROCESS_TEXT
        selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
        isReadOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)

        if (selectedText.isBlank()) {
            finish()
            return
        }

        binding.tvOriginalText.text = selectedText

        // Configure UI Buttons
        if (!isReadOnly) {
            binding.btnReplaceText.visibility = View.VISIBLE
            binding.btnReplaceText.setOnClickListener {
                replaceSelectedTextAndFinish()
            }
        } else {
            binding.btnReplaceText.visibility = View.GONE
        }

        binding.btnCopyText.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Translated Text", binding.tvTranslatedText.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

        setupSpinners()
    }

    private fun setupSpinners() {
        val sourceNames = mutableListOf(getString(R.string.auto_detect))
        sourceNames.addAll(langOptions.map { it.name })

        val sourceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sourceNames)
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSourceLang.adapter = sourceAdapter

        val targetNames = langOptions.map { it.name }
        val targetAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, targetNames)
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTargetLang.adapter = targetAdapter

        // Default: Hebrew target
        val defaultHebrewIndex = langOptions.indexOfFirst { it.code == TranslateLanguage.HEBREW }
        if (defaultHebrewIndex != -1) {
            binding.spinnerTargetLang.setSelection(defaultHebrewIndex)
        }

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sourcePos = binding.spinnerSourceLang.selectedItemPosition
                selectedSourceLangCode = if (sourcePos == 0) "AUTO" else langOptions[sourcePos - 1].code
                
                val targetPos = binding.spinnerTargetLang.selectedItemPosition
                selectedTargetLangCode = langOptions[targetPos].code

                performTranslation()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerSourceLang.onItemSelectedListener = listener
        binding.spinnerTargetLang.onItemSelectedListener = listener
    }

    private fun performTranslation() {
        binding.tvTranslatedText.text = getString(R.string.translating)

        if (selectedSourceLangCode == "AUTO") {
            // Auto detect source language using ML Kit Language Identification
            val languageIdentifier = LanguageIdentification.getClient()
            languageIdentifier.identifyLanguage(selectedText)
                .addOnSuccessListener { languageCode ->
                    val resolvedSource = if (languageCode == "und" || languageCode.length < 2) {
                        // Fallback: Default to English if detection fails
                        TranslateLanguage.ENGLISH
                    } else {
                        TranslateLanguage.fromLanguageTag(languageCode) ?: TranslateLanguage.ENGLISH
                    }
                    translateText(resolvedSource, selectedTargetLangCode)
                }
                .addOnFailureListener {
                    translateText(TranslateLanguage.ENGLISH, selectedTargetLangCode)
                }
        } else {
            translateText(selectedSourceLangCode, selectedTargetLangCode)
        }
    }

    private fun translateText(sourceLang: String, targetLang: String) {
        currentTranslator?.close()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        val translator = Translation.getClient(options)
        currentTranslator = translator

        translator.translate(selectedText)
            .addOnSuccessListener { translatedText ->
                binding.tvTranslatedText.text = translatedText
            }
            .addOnFailureListener { e ->
                binding.tvTranslatedText.text = getString(R.string.model_not_downloaded)
            }
    }

    private fun replaceSelectedTextAndFinish() {
        val translated = binding.tvTranslatedText.text.toString()
        if (translated.isNotBlank() && translated != getString(R.string.translating) && translated != getString(R.string.model_not_downloaded)) {
            val resultIntent = Intent()
            resultIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, translated)
            setResult(Activity.RESULT_OK, resultIntent)
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentTranslator?.close()
    }
}
