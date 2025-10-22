package com.example.leagueguessr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter_History(
    val historyItems: List<Data_history>
) : RecyclerView.Adapter<Adapter_History.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val championImage: ImageView = itemView.findViewById(R.id.ivHistoryChampion)
        val resultText: TextView = itemView.findViewById(R.id.tvHistoryResult)
        val dateText: TextView = itemView.findViewById(R.id.tvHistoryDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyItems[position]

        holder.championImage.setImageResource(historyItem.championImageResId)
        holder.resultText.text = historyItem.result
        holder.dateText.text = historyItem.date
    }

    override fun getItemCount(): Int = historyItems.size
}