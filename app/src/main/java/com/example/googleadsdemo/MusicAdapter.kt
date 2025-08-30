package com.example.googleadsdemo

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MusicAdapter(
    private val musicList: List<MusicModel>,
    private val onItemClick: (MusicModel) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
        val artist = itemView.findViewById<TextView>(R.id.artist)
        val poster: ImageView = itemView.findViewById(R.id.imgposter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.title.text = music.title
        holder.artist.text = music.artist

        Glide.with(holder.itemView.context)
            .load(music.albumArtUri)
            .placeholder(R.drawable.background) // default if no poster
            .into(holder.poster)

        holder.itemView.setOnClickListener {

            Log.d("TAG", "Clicked: ${music.title}")
            val context = holder.itemView.context

            // Create an ArrayList of all song paths
            val songPaths = ArrayList<String>()
            for (item in musicList) {
                songPaths.add(item.path)
            }

            // Pass full list and clicked index
            val intent = Intent(context, Mp3Activity::class.java)
            intent.putStringArrayListExtra("song_list", songPaths)
            intent.putExtra("song_index", position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = musicList.size
}
