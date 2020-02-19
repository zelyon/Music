package bzh.zelyon.music.util

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AlertDialog
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import java.io.File

object MusicPlayer: MediaPlayer() {

    private var lastCurrentPosition = 0
    val listeners = mutableListOf<Listener>()
    var musics = mutableListOf<Music>()
    var currentMusic: Music? = null
    var currentMusicPosition = 0

    fun playMusics(musics: List<Music>) {
        this.musics = musics.toMutableList()
        currentMusicPosition = 0
        setOnCompletionListener { next() }
        playMusic()
    }

    fun addMusics(musics: List<Music>) {
        this.musics.addAll(musics)
    }

    private fun playMusic() {
        if (currentMusicPosition in musics.indices) {
            val music = musics[currentMusicPosition]
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
        currentMusicPosition = 0
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
        currentMusicPosition = if (currentMusicPosition < 1) 0 else currentMusicPosition - 1
        playMusic()
    }

    fun next() {
        updateMusicIndex()
        currentMusicPosition++
        playMusic()
    }

    fun shuffle() {
        updateMusicIndex()
        musics.shuffle()
        currentMusic = musics[currentMusicPosition]
        playMusic()
    }

    fun updateMusicIndex() {
        currentMusicPosition = if (musics.contains(currentMusic)) musics.indexOf(currentMusic) else currentMusicPosition
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