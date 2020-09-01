package com.example.dmusicplayer

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dmusicplayer.model.SongInfo
import kotlinx.coroutines.launch

class SongsViewModel(application: Application) : AndroidViewModel(application), Observable {

    private val context = getApplication<Application>().applicationContext

    var songsList = MutableLiveData<ArrayList<SongInfo>>()

    @Bindable
    fun getSongs() : LiveData<ArrayList<SongInfo>> {
        return songsList
    }

    fun setSongsInAdapter(songsList: ArrayList<SongInfo>) {

    }

    fun loadSongs() = viewModelScope.launch {
        val songs = ArrayList<SongInfo>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = context.contentResolver.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val albumID = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val songURI = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                var albumPath = ""

                val albumCursor: Cursor? = context.contentResolver.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums._ID + "=?",
                    arrayOf(albumID.toString()),
                    null
                )

                if (albumCursor != null) {
                    if (albumCursor.moveToFirst()) {
                        albumPath = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                    }
                }

                /*val albumArtPath = if(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART) != -1 )
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)) else ""*/
                val albumURI = ContentUris.withAppendedId(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    albumID.toLong()
                )
                songs.add(SongInfo(title, artist, album, duration, albumPath, albumURI, songURI))
            }
            songsList.value = songs
        }
        cursor?.close()
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }
}