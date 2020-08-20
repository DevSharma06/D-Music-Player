package com.example.dmusicplayer

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.*
import com.example.dmusicplayer.model.SongInfo
import kotlinx.coroutines.launch

class SongsViewModel(application: Application) : AndroidViewModel(application), Observable {

    private val context = getApplication<Application>().applicationContext

    var songsList = MutableLiveData<ArrayList<SongInfo>>()

    @Bindable
    fun getSongs() : LiveData<ArrayList<SongInfo>> {
        return songsList
    }

    fun setSongsInAdapter(songsList : ArrayList<SongInfo>) {

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
                val albumArtPath = if(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART) != -1 )
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)) else ""
                val albumURI = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumID.toLong())
                songs.add(SongInfo(title, artist, album, duration, albumArtPath, albumURI))
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