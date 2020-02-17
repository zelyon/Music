package bzh.zelyon.music.utils

import android.app.PendingIntent
import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.media.MediaBrowserServiceCompat
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Artist
import bzh.zelyon.music.db.model.Music
import java.io.File

object MusicManager {

    private val mediaPlayer = MediaPlayer()
    private var lastCurrentPosition = 0
    val isPlaying: Boolean get() = mediaPlayer.isPlaying
    val currentPosition: Int get() = mediaPlayer.currentPosition
    val duration: Int get() = mediaPlayer.duration
    val previousExist: Boolean get() = musicPosition > 0
    val nextExist: Boolean get() = musicPosition < musics.size - 1
    val listeners = mutableListOf<Listener>()
    var musicPosition = 0
    var musics = mutableListOf<Music>()
    var currentMusic: Music? = null
    var isPlayingOrPause = false

    fun getMusics(context: Context, query: String = "1 = 1", vararg args: String): List<Music> {
        val musics = mutableListOf<Music>()
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.DATA
        )
        val selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " = 1 AND ($query)"
        val order = MediaStore.Audio.AudioColumns.ARTIST + ", " +
                MediaStore.Audio.AudioColumns.YEAR + "," +
                MediaStore.Audio.AudioColumns.ALBUM + ", " +
                MediaStore.Audio.AudioColumns.TRACK  + ", " +
                MediaStore.Audio.AudioColumns.TITLE +
                " COLLATE NOCASE ASC"
        context.contentResolver?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, args, order)?.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex(MediaStore.Audio.Media._ID))
                val title = it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artistId = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                val artistName = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val albumId = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val albumName = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val track = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.TRACK))
                val year = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.YEAR))
                val duration = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val path = it.getString(it.getColumnIndex(MediaStore.Audio.Media.DATA))
                if (File(path).exists()) {
                    musics.add(Music(id, title, artistId, artistName, albumId, albumName, track, year, duration, path))
                }
            }
            it.close()
        }
        return musics
    }

    fun getMusicsBySearch(context: Context, search: String): List<Artist> {
        val artists = mutableMapOf<Int, Artist>()
        val query = MediaStore.Audio.AudioColumns.TITLE + " LIKE ? OR " +
                MediaStore.Audio.AudioColumns.ARTIST + " LIKE ? OR " +
                MediaStore.Audio.AudioColumns.ALBUM + " LIKE ? OR " +
                MediaStore.Audio.AudioColumns.YEAR + " LIKE ? "
        getMusics(context, query, "%$search%", "%$search%", "%$search%", "%$search%").forEach { music ->
            if (artists.containsKey(music.artistId)) {
                artists.getValue(music.artistId).musics.add(music)
            } else {
                artists[music.artistId] = Artist(music.artistId, music.artistName, mutableListOf(music))
            }
        }
        return artists.values.toList()
    }

    fun getMusicFromUri(context: Context, uri: Uri): Music? {
        var path = uri.path
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            context.contentResolver?.query(uri, projection, null, null, null)?.use {
                if (it.moveToFirst()) {
                    path = it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
                }
                it.close()
            }
        }
        return path?.let { getMusics(context, MediaStore.Audio.AudioColumns.DATA + " = ? ", it).firstOrNull() }
    }

    fun playMusics(musics: List<Music>) {
        MusicManager.musics = musics.toMutableList()
        musicPosition = 0
        mediaPlayer.setOnCompletionListener { next() }
        playMusic()
    }

    fun addMusics(musics: List<Music>) {
        MusicManager.musics.addAll(musics)
    }

    fun deleteMusicFile(context: Context, music: Music) {
        AlertDialog.Builder(context)
            .setTitle(R.string.item_popup_delete_title)
            .setMessage(context.getString(R.string.item_popup_delete_message, music.title))
            .setPositiveButton(R.string.item_popup_delete_positive) { _, _ ->
                File(music.path).delete()
                listeners.forEach {
                    it.onMusicFileDeleted(music)
                }
            }
            .show()
    }

    private fun playMusic() {
        if (musicPosition in musics.indices) {
            val music = musics[musicPosition]
            if (File(music.path).exists()) {
                isPlayingOrPause = true
                currentMusic = music
                mediaPlayer.reset()
                mediaPlayer.setDataSource(music.path)
                mediaPlayer.prepare()
                mediaPlayer.start()
            } else {
                stop()
            }
        } else {
            stop()
        }
    }

    fun pauseOrPlay() {
        if (isPlaying) {
            lastCurrentPosition = currentPosition
            mediaPlayer.pause()
        } else {
            mediaPlayer.seekTo(lastCurrentPosition)
            mediaPlayer.start()
        }
    }

    fun goTo(current: Int) {
        lastCurrentPosition = current
        mediaPlayer.seekTo(current)
    }

    fun stop() {
        isPlayingOrPause = false
        musics.clear()
        musicPosition = 0
        currentMusic = null
        mediaPlayer.setOnCompletionListener {}
        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    fun previous() {
        updateMusicIndex()
        musicPosition = if (musicPosition < 1) 0 else musicPosition - 1
        playMusic()
    }

    fun next() {
        updateMusicIndex()
        musicPosition++
        playMusic()
    }

    fun jumpTo(music: Music) {
        currentMusic = music
        updateMusicIndex()
        playMusic()
    }

    fun shuffle() {
        musics.shuffle()
        playMusic()
    }

    fun updateMusicIndex() {
        musicPosition = if (musics.contains(currentMusic)) musics.indexOf(currentMusic) else musicPosition
    }

    interface Listener {
        fun onMusicFileDeleted(music: Music)
    }

    class Service: MediaBrowserServiceCompat() {

        override fun onCreate() {
            super.onCreate()

            val componentName = ComponentName(applicationContext, Receiver::class.java)
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON).setComponent(componentName)
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
            val mediaSessionCompat = MediaSessionCompat(this, getString(R.string.app_name), componentName, pendingIntent).apply {
                setMediaButtonReceiver(pendingIntent)
                setCallback(MediaSessionCallback(applicationContext))
                isActive = true
            }
            sessionToken = mediaSessionCompat.sessionToken
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            when(intent?.action?.toInt()) {
                KeyEvent.KEYCODE_MEDIA_STOP -> stop()
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> pauseOrPlay()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> previous()
                KeyEvent.KEYCODE_MEDIA_NEXT -> next()
            }
            return super.onStartCommand(intent, flags, startId)
        }

        override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
            result.sendResult(null)
        }

        override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?) = BrowserRoot(getString(
            R.string.app_name
        ), null)

        private class MediaSessionCallback(val context: Context): MediaSessionCompat.Callback() {
            override fun onPlay() {
                pauseOrPlay()
            }
            override fun onPause() {
                pauseOrPlay()
            }
            override fun onSkipToNext() {
                next()
            }
            override fun onSkipToPrevious() {
                previous()
            }
            override fun onStop() {
                stop()
            }
            override fun onSeekTo(pos: Long) {
                goTo(pos.toInt())
            }
            override fun onMediaButtonEvent(mediaButtonEvent: Intent) = Receiver.parseIntent(context, mediaButtonEvent)
        }
    }

    class Receiver: BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (parseIntent(context, intent) && isOrderedBroadcast) {
                abortBroadcast()
            }
        }

        companion object {
            fun parseIntent(context: Context, intent: Intent): Boolean {
                intent.getParcelableExtra<KeyEvent?>(Intent.EXTRA_KEY_EVENT)?.let { event ->
                    if (intent.action == Intent.ACTION_MEDIA_BUTTON && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        context.startService(Intent(context, Service::class.java).setAction(event.keyCode.toString()))
                        return true
                    }
                }
                return false
            }
        }
    }
}