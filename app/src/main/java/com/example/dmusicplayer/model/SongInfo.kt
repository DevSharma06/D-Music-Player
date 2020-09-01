package com.example.dmusicplayer.model

import android.net.Uri

data class SongInfo(
    var songTitle: String,
    var artist: String,
    var album: String,
    var duration: String,
    var albumArtPath : String,
    var albumURI: Uri,
    var songURI: String
)