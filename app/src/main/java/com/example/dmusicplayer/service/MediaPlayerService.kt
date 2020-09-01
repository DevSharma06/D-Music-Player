package com.example.dmusicplayer.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import java.lang.Exception

class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaFile: String
    private var resumePosition = 0

    private lateinit var audioManager: AudioManager

    private var audioAttributes =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        } else {
            null
        }

    private val audioFocusRequest =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes!!)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
        } else {
            null
        }

    private val iBinder = LocalBinder()

    override fun onBind(p0: Intent?): IBinder? {
        return iBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            mediaFile = intent!!.extras!!.getString("media_path", "")
        } catch (e : Exception) {
            stopSelf()
        }

        if (!requestAudioFocus()) {
            stopSelf()
        }

        if (!mediaFile.isNullOrEmpty()) {
            initMediaPlayer()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia()
            mediaPlayer.release()
        }
        removeAudioFocus()
    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    override fun onCompletion(p0: MediaPlayer?) {
        stopMedia()
        stopSelf()
    }

    override fun onPrepared(p0: MediaPlayer?) {
        playMedia()
    }

    override fun onAudioFocusChange(p0: Int) {
        when (p0) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer == null)
                    initMediaPlayer()
                else if (!mediaPlayer.isPlaying)
                    mediaPlayer.start()
                mediaPlayer.setVolume(1.0F, 1.0F)
            }

            // Lost focus for an unbounded amount of time: stop playback and release media player
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer.isPlaying)
                    mediaPlayer.stop()
                mediaPlayer.release()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback is likely to resume
                if (mediaPlayer.isPlaying)
                    mediaPlayer.pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying)
                    mediaPlayer.setVolume(0.1f, 0.1f)
            }

        }
    }

    fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(
                audioFocusRequest!!
            )
        } else {
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true
        }
        return false
    }

    fun removeAudioFocus(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocusRequest(
                audioFocusRequest!!
            )
        } else {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)
        }
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        when (p1) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ->
                Log.d("MediaPlayer error:", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $p2")
            MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                Log.d("MediaPlayer error:", "MEDIA ERROR SERVER DIED $p2")
            MediaPlayer.MEDIA_ERROR_UNKNOWN ->
                Log.d("MediaPlayer error:", "MEDIA ERROR UNKNOWN $p2")
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED ->
                Log.d("MediaPlayer error:", "MEDIA ERROR UNSUPPORTED $p2")
        }
        return false;
    }

    override fun onSeekComplete(p0: MediaPlayer?) {
        TODO("Not yet implemented")
    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        //Invoked to communicate some info.
        return false;
    }

    class LocalBinder : Binder() {
        fun getService(): MediaPlayerService {
            return MediaPlayerService()
        }
    }

    fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnBufferingUpdateListener(this)
        mediaPlayer.setOnSeekCompleteListener(this)
        mediaPlayer.setOnInfoListener(this)

        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(audioAttributes)
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }

        try {
            mediaPlayer.setDataSource(mediaFile)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
        mediaPlayer.prepareAsync()
    }

    fun playMedia() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun pauseMedia() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            resumePosition = mediaPlayer.currentPosition
        }
    }

    fun resumeMedia() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(resumePosition)
            mediaPlayer.start()
        }
    }

}