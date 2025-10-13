package com.example.leagueguessr

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ChampionsAdapter(
    val champions: List<Data_champion>,
    val onChampionClick: (Data_champion) -> Unit
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

        holder.itemView.setOnLongClickListener {
            openOpGG(champion.name, holder.itemView.context)
            true
        }
    }

    override fun getItemCount(): Int = champions.size

    private fun openOpGG(championName: String, context: android.content.Context) {

            val formattedName = formatChampionNameForOpGG(championName)
            val url = "https://op.gg/lol/champions/$formattedName"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
    }

    private fun formatChampionNameForOpGG(championName: String): String {
        return championName.lowercase()
            .replace(" ", "")
            .replace("nunuandwillump", "nunu")
            .replace(".", "")
    }
}