package com.example.dmusicplayer.adapter

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dmusicplayer.R
import com.example.dmusicplayer.databinding.SongsListItemBinding
import com.example.dmusicplayer.model.SongInfo

class SongsRecyclerViewAdapter(private val clickListener: (Int) -> Unit)
    : RecyclerView.Adapter<SongsRecyclerViewAdapter.SongsViewHolder>() {

    private val songsList = ArrayList<SongInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: SongsListItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.songs_list_item, parent, false)
        return SongsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        holder.bind(position, songsList[position], clickListener)
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    fun setList(songs : ArrayList<SongInfo>) {
        songsList.clear()
        songsList.addAll(songs)
    }

    class SongsViewHolder(private val binding: SongsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, song: SongInfo, clickListener: (Int) -> Unit) {
            binding.apply {
                tvSongName.text = song.songTitle
                tvArtistName.text = song.artist

                val minutes = (song.duration.toLong() / 1000) / 60
                val seconds = (song.duration.toLong() / 1000) % 60
                tvDuration.text = "${minutes}m ${seconds}s"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                } else {
                    if (song.albumArtPath.isNotEmpty()) {
                        Glide.with(ivAlbumIcon).load(song.albumArtPath).into(ivAlbumIcon)
//                        val image = Drawable.createFromPath(song.albumArtPath)
//                        ivAlbumIcon.setImageDrawable(image)
                    }
                }

                songLayout.setOnClickListener {
                    clickListener(position)
                }
            }
        }
    }
}