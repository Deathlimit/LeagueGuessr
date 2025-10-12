package com.example.leagueguessr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChampionsAdapter(
    private val champions: List<Champion>,
    private val onChampionClick: (Champion) -> Unit
) : RecyclerView.Adapter<ChampionsAdapter.ChampionViewHolder>() {


    class ChampionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivChampion)
        val textView: TextView = itemView.findViewById(R.id.tvChampionName)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChampionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_champion, parent, false)
        return ChampionViewHolder(view)
    }


    override fun onBindViewHolder(holder: ChampionViewHolder, position: Int) {
        val champion = champions[position]

        holder.imageView.setImageResource(champion.imageResId)
        holder.textView.text = champion.name


        holder.itemView.setOnClickListener {
            onChampionClick(champion)
        }
    }


    override fun getItemCount(): Int = champions.size
}