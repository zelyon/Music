package bzh.zelyon.music.util

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AlertDialog
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.util.MusicPlayer.setOnCompletionListener
import java.io.File

object MusicPlayer: MediaPlayer() {

    private var lastCurrentPosition = 0
    val listeners = mutableListOf<Listener>()
    var musics = mutableListOf<Music>()
    var musicPosition = 0
    var currentMusic: Music? = null

    fun playMusics(musics: List<Music>) {
        this.musics = musics.toMutableList()
        musicPosition = 0
        setOnCompletionListener { next() }
        playMusic()
    }

    fun addMusics(musics: List<Music>) {
        this.musics.addAll(musics)
    }

    private fun playMusic() {
        if (musicPosition in musics.indices) {
            val music = musics[musicPosition]
            if (File(music.path).exists()) {
                currentMusic = music
                reset()
                setDataSource(music.path)
                prepare()
                start()
            } else {
                cancel()
            }
        } else {
            cancel()
        }
    }

    private fun cancel() {
        musics.clear()
        musicPosition = 0
        currentMusic = null
        setOnCompletionListener {}
        stop()
        reset()
    }

    fun pauseOrPlay() {
        if (isPlaying) {
            lastCurrentPosition = currentPosition
            pause()
        } else {
            seekTo(lastCurrentPosition)
            start()
        }
    }

    fun goTo(current: Int) {
        lastCurrentPosition = current
        seekTo(current)
    }

    fun jumpTo(music: Music) {
        currentMusic = music
        updateMusicIndex()
        playMusic()
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

    fun shuffle() {
        musics.shuffle()
        playMusic()
    }

    fun updateMusicIndex() {
        musicPosition = if (musics.contains(currentMusic)) musics.indexOf(currentMusic) else musicPosition
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

    interface Listener {
        fun onMusicFileDeleted(music: Music)
    }
}