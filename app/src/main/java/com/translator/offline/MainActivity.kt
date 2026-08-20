package com.translator.offline

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.translator.offline.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LanguageAdapter
    private val modelManager = RemoteModelManager.getInstance()
    private val languageList = SupportedLanguages.languages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LanguageAdapter(languageList) { item ->
            if (item.isDownloaded) {
                deleteModel(item)
            } else {
                downloadModel(item)
            }
        }
        binding.rvLanguageModels.adapter = adapter

        checkDownloadedModels()
    }

    private fun checkDownloadedModels() {
        modelManager.getModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                val downloadedCodes = models.map { it.language }
                languageList.forEach { lang ->
                    lang.isDownloaded = downloadedCodes.contains(lang.code)
                    lang.isDownloading = false
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בטעינת סטטוס המילונים", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadModel(item: LanguageModelItem) {
        item.isDownloading = true
        adapter.notifyDataSetChanged()

        val model = TranslateRemoteModel.Builder(item.code).build()
        val conditions = DownloadConditions.Builder().build() // Allow download on cellular or wifi

        modelManager.download(model, conditions)
        .addOnSuccessListener {
            item.isDownloading = false
            item.isDownloaded = true
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "מילון ${item.name} הורד בהצלחה!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            item.isDownloading = false
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "הורדה נכשלה: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteModel(item: LanguageModelItem) {
        val model = TranslateRemoteModel.Builder(item.code).build()
        modelManager.deleteDownloadedModel(model)
            .addOnSuccessListener {
                item.isDownloaded = false
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "מילון ${item.name} נמחק", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "מחיקה נכשלה: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
