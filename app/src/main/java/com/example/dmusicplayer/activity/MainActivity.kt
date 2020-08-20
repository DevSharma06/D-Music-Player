package com.example.dmusicplayer.activity

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(SongsViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

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

    }

    private fun loadSongsFromStorage() {
        viewModel.loadSongs()
        viewModel.getSongs().observe(this, Observer {
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