package com.example.dmusicplayer.activity

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dmusicplayer.R
import com.example.dmusicplayer.SongsViewModel
import com.example.dmusicplayer.model.SongInfo
import com.example.dmusicplayer.adapter.SongsRecyclerViewAdapter
import com.example.dmusicplayer.databinding.ActivityMainBinding
import com.example.dmusicplayer.util.Utils

class MainActivity : AppCompatActivity() {

    private val songsList = ArrayList<SongInfo>()

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SongsViewModel

    private lateinit var songsAdapter: SongsRecyclerViewAdapter

    private val PERMISSION_REQUEST_CODE = 1878
    private val permissions = arrayOf<String>( android.Manifest.permission.READ_EXTERNAL_STORAGE)

    private lateinit var mediaPlayer : MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(SongsViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        mediaPlayer = MediaPlayer()

        setRecyclerViewAdapter()
    }

    override fun onStart() {
        super.onStart()

        if (!Utils.checkPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            loadSongsFromStorage()
        }
    }

    private fun setRecyclerViewAdapter() {
        songsAdapter = SongsRecyclerViewAdapter({position: Int -> onSongClicked(position)})
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = songsAdapter
    }

    private fun onSongClicked(position: Int) {
        binding.apply {
            bottomBar.visibility = View.VISIBLE
            Glide.with(this@MainActivity).load(songsList[position].albumArtPath).into(ivAlbumArt)
            tvSongTitle.text = songsList[position].songTitle
            tvArtistName.text = songsList[position].artist

            playSong(position)
        }
    }

    private fun playSong(position: Int) {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(songsList[position].songURI)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                it.start()
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSongsFromStorage() {
        viewModel.loadSongs()
        viewModel.getSongs().observe(this, Observer {
            songsList.addAll(it)
            songsAdapter.setList(it)
            songsAdapter.notifyDataSetChanged()
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size == 1) {
            loadSongsFromStorage()
        }
    }
}