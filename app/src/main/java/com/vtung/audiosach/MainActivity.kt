package com.vtung.audiosach

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var tvContent: TextView
    private val bookManager = BookManager()
    private var currentDoc: PDDocument? = null
    private var currentText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        tvContent = TextView(this).apply { textSize = 18f }
        layout.addView(Button(this).apply {
            text = "Chọn PDF"
            setOnClickListener { 
                startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }, 100) 
            }
        })
        layout.addView(ScrollView(this).apply { addView(tvContent) })
        setContentView(layout)
        tts = TextToSpeech(this, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            CoroutineScope(Dispatchers.IO).launch {
                currentDoc = PDDocument.load(contentResolver.openInputStream(uri))
                val text = bookManager.getPageText(currentDoc!!, 0)
                withContext(Dispatchers.Main) { 
                    currentText = text
                    tvContent.text = text
                    speakText(text)
                }
            }
        }
    }

    private fun speakText(text: String) {
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "karaoke") }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "karaoke")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("vi", "VN")
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onRangeStart(uId: String?, start: Int, end: Int, frame: Int) {
                    runOnUiThread {
                        val spannable = SpannableString(currentText)
                        spannable.setSpan(BackgroundColorSpan(0xFFFFF176.toInt()), start, end, 0)
                        tvContent.text = spannable
                    }
                }
                override fun onStart(i: String?) {}
                override fun onDone(i: String?) {}
                override fun onError(i: String?) {}
            })
        }
    }
}
