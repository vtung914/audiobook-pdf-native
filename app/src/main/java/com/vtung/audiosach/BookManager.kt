package com.vtung.audiosach

import android.util.LruCache
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

class BookManager {
    private val pageCache = LruCache<Int, String>(50)

    fun getPageText(doc: PDDocument, pageIndex: Int): String {
        pageCache.get(pageIndex)?.let { return it }
        val stripper = PDFTextStripper()
        stripper.startPage = pageIndex + 1
        stripper.endPage = pageIndex + 1
        val text = stripper.getText(doc)
        pageCache.put(pageIndex, text)
        return text
    }
}
