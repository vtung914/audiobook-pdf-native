package com.vtung.audiosach

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {
    private lateinit var tvContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvContent = findViewById(R.id.tvContent)
        val btnSelect = findViewById<Button>(R.id.btnSelect)

        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/epub+zip" }
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            readEpub(data.data!!)
        }
    }

    private fun readEpub(uri: android.net.Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val zipStream = ZipInputStream(inputStream)
            var entry = zipStream.nextEntry
            while (entry != null) {
                // Đọc file nội dung (thường có đuôi .html hoặc .xhtml)
                if (entry.name.endsWith(".html") || entry.name.endsWith(".xhtml")) {
                    val content = zipStream.bufferedReader().readText()
                    val text = org.jsoup.Jsoup.parse(content).text()
                    runOnUiThread { tvContent.text = text }
                    break // Đọc chương đầu tiên
                }
                entry = zipStream.nextEntry
            }
        }
    }
}
