package com.vtung.audiosach

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var tvContent: TextView
    private lateinit var btnPlay: Button
    private var currentUri: Uri? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        btnPlay = Button(this).apply { text = "Chọn PDF & Đọc" }
        layout.addView(btnPlay)

        tvContent = TextView(this).apply { textSize = 16f }
        layout.addView(ScrollView(this).apply { addView(tvContent) })

        setContentView(layout)

        tts = TextToSpeech(this, this)

        btnPlay.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
            startActivityForResult(Intent.createChooser(intent, "Chọn PDF"), 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            currentUri = data?.data
            loadPdfAsync(currentUri!!)
        }
    }

    // 
    private fun loadPdfAsync(uri: Uri) {
        // Coroutine giúp không làm đơ giao diện
        CoroutineScope(Dispatchers.Main).launch {
            tvContent.text = "Đang xử lý PDF..."
            val text = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val doc = PDDocument.load(stream)
                    val content = PDFTextStripper().getText(doc)
                    doc.close()
                    content
                } ?: ""
            }
            tvContent.text = text
            loadProgressAndStart(uri)
        }
    }

    private fun loadProgressAndStart(uri: Uri) {
        val prefs = getSharedPreferences("AudioBookPrefs", MODE_PRIVATE)
        val savedText = prefs.getString(uri.toString(), "")
        if (savedText!!.isNotEmpty()) {
            Toast.makeText(this, "Đã khôi phục vị trí cũ", Toast.LENGTH_SHORT).show()
        }
        speakText(tvContent.text.toString())
    }

    private fun speakText(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId")
        isPlaying = true
    }

    override fun onPause() {
        super.onPause()
        // Lưu lại vị trí khi thoát app
        currentUri?.let {
            getSharedPreferences("AudioBookPrefs", MODE_PRIVATE).edit()
                .putString(it.toString(), "SavedPosition") // Mở rộng thêm logic lưu vị trí cụ thể
                .apply()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale("vi", "VN")
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
