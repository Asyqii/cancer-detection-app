package com.dicoding.asclepius.ui.history

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.R
import com.dicoding.asclepius.database.History

class HistoryAdapter(private val historyList : List<History>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {


    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.tv_item_published_date)
        val resultTextView: TextView = view.findViewById(R.id.tv_result)
        val confidenceTextView: TextView = view.findViewById(R.id.tv_item_score)
        val image: ImageView = view.findViewById(R.id.img_poster)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]
        holder.dateTextView.text = history.date
        holder.resultTextView.text = history.result
        holder.confidenceTextView.text = "${"%.2f".format(history.confidenceScore * 100)}%"
        Glide.with(holder.image.context).load(history.image).into(holder.image)
        Log.d("HistoryAdapter", "Cek History Image : ${history.image}, Holder : ${holder.image}")
    }

    override fun getItemCount(): Int = historyList.size

}