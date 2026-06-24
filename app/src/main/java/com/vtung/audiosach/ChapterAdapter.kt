package com.vtung.audiosach
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.siegmann.epublib.domain.SpineReference

class ChapterAdapter(private val chapters: List<SpineReference>, private val onClick: (Int) -> Unit) : 
    RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) { val tv: TextView = view.findViewById(android.R.id.text1) }
    override fun onCreateViewHolder(parent: ViewGroup, v: Int) = ViewHolder(android.widget.ArrayAdapter<String>(parent.context, android.R.layout.simple_list_item_1).view)
    override fun onBindViewHolder(h: ViewHolder, p: Int) {
        h.tv.text = "Chương ${p + 1}"
        h.itemView.setOnClickListener { onClick(p) }
    }
    override fun getItemCount() = chapters.size
}
