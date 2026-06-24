package com.vtung.audiosach

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import nl.siegmann.epublib.epub.EpubReader
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var tvContent: TextView
    private var currentText: String = ""
    private var currentBook: nl.siegmann.epublib.domain.Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvContent = findViewById(R.id.tvContent)
        val btnSelect = findViewById<Button>(R.id.btnSelect)
        val rvChapters = findViewById<RecyclerView>(R.id.rvChapters)
        rvChapters.layoutManager = LinearLayoutManager(this)

        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/epub+zip" }
            startActivityForResult(intent, 100)
        }

        tts = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) tts.language = Locale("vi", "VN") }
        
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onRangeStart(uId: String?, start: Int, end: Int, frame: Int) {
                runOnUiThread {
                    if (start >= 0 && end <= currentText.length) {
                        val span = SpannableString(currentText)
                        span.setSpan(BackgroundColorSpan(Color.YELLOW), start, end, 0)
                        tvContent.text = span
                    }
                }
            }
            override fun onStart(i: String?) {}
            override fun onDone(i: String?) {}
            override fun onError(i: String?) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val stream = contentResolver.openInputStream(data.data!!)
                currentBook = EpubReader().readEpub(stream)
                val chapters = currentBook!!.spine.spineReferences
                withContext(Dispatchers.Main) {
                    findViewById<RecyclerView>(R.id.rvChapters).adapter = ChapterAdapter(chapters) { loadChapter(it) }
                }
            }
        }
    }

    private fun loadChapter(index: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val resource = currentBook!!.spine.spineReferences[index].resource
            val text = org.jsoup.Jsoup.parse(String(resource.data)).text()
            withContext(Dispatchers.Main) {
                currentText = text
                tvContent.text = text
                val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id") }
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "id")
            }
        }
    }
}
