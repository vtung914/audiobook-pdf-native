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
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var tvContent: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var btnPick: Button
    private lateinit var btnPlay: Button
    private lateinit var sbSpeed: SeekBar

    private var fullText = ""
    private val sentences = ArrayList<String>()
    private val sentenceOffsets = ArrayList<Int>()
    private var currentSentenceIndex = 0
    private var playbackSpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))
        }

        val tvTitle = TextView(this).apply {
            text = "AUDIO BOOK PDF NATIVE"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#FFC107"))
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(tvTitle)

        btnPick = Button(this).apply { text = "📁 CHỌN FILE PDF TRUYỆN" }
        layout.addView(btnPick)

        val tvSpeedLabel = TextView(this).apply {
            text = "Tốc độ đọc: 1.0x"
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(tvSpeedLabel)

        sbSpeed = SeekBar(this).apply {
            max = 15
            progress = 5
        }
        layout.addView(sbSpeed)

        btnPlay = Button(this).apply { 
            text = "▶️ PHÁT AUDIO"
            isEnabled = false 
        }
        layout.addView(btnPlay)

        scrollView = ScrollView(this)
        tvContent = TextView(this).apply {
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(20, 20, 20, 20)
        }
        scrollView.addView(tvContent)
        layout.addView(scrollView)

        setContentView(layout)

        tts = TextToSpeech(this, this)
        PDFBoxResourceLoader.init(applicationContext)

        btnPick.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Chọn file PDF"), 100)
        }

        btnPlay.setOnClickListener {
            if (tts.isSpeaking) {
                tts.stop()
                btnPlay.text = "▶️ PHÁT AUDIO"
            } else {
                speakFromIndex(currentSentenceIndex)
                btnPlay.text = "⏹️ DỪNG PHÁT"
            }
        }

        sbSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                playbackSpeed = 0.5f + (progress / 10.0f)
                tvSpeedLabel.text = "Tốc độ đọc: ${String.format("%.1f", playbackSpeed)}x"
                tts.setSpeechRate(playbackSpeed)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("vi", "VN")
            setupTTSListener()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> extractTextFromPdf(uri) }
        }
    }

    private fun extractTextFromPdf(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                fullText = stripper.getText(document)
                document.close()

                if (fullText.trim().length > 5) {
                    processTextIntoSentences(fullText)
                    tvContent.text = fullText
                    btnPlay.isEnabled = true
                    Toast.makeText(this, "Đã tải xong dữ liệu truyện!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi đọc PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun processTextIntoSentences(text: String) {
        sentences.clear()
        sentenceOffsets.clear()
        val rawSentences = text.split(Regex("(?<=[.!?])\\s+"))
        var currentOffset = 0
        for (sentence in rawSentences) {
            if (sentence.trim().isNotEmpty()) {
                sentences.add(sentence)
                sentenceOffsets.add(text.indexOf(sentence, currentOffset))
                currentOffset = sentenceOffsets.last() + sentence.length
            }
        }
    }

    private fun speakFromIndex(index: Int) {
        if (index >= sentences.size) {
            btnPlay.text = "▶️ PHÁT AUDIO"
            return
        }
        currentSentenceIndex = index
        tts.setSpeechRate(playbackSpeed)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, index.toString())
        }
        tts.speak(sentences[index], TextToSpeech.QUEUE_FLUSH, params, index.toString())
    }

    private fun setupTTSListener() {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val index = utteranceId?.toIntOrNull() ?: return
                runOnUiThread { highlightSentence(index) }
            }
            override fun onDone(utteranceId: String?) {
                val index = utteranceId?.toIntOrNull() ?: return
                speakFromIndex(index + 1)
            }
            override fun onError(utteranceId: String?) {}
        })
    }

    private fun highlightSentence(index: Int) {
        val startPos = sentenceOffsets[index]
        val endPos = startPos + sentences[index].length
        val spannable = SpannableString(fullText)
        spannable.setSpan(
            BackgroundColorSpan(android.graphics.Color.parseColor("#55BB8800")),
            startPos, endPos, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvContent.text = spannable
        val layout = tvContent.layout
        if (layout != null) {
            val line = layout.getLineForOffset(startPos)
            val y = layout.getLineTop(line)
            scrollView.smoothScrollTo(0, y)
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        super.onDestroy()
    }
}
