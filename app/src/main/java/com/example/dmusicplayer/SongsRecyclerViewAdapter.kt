package com.example.dmusicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dmusicplayer.databinding.SongsListItemBinding

class SongsRecyclerViewAdapter : RecyclerView.Adapter<SongsRecyclerViewAdapter.SongsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: SongsListItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.songs_list_item, parent, false)
        return SongsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    class SongsViewHolder(private val binding: SongsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}