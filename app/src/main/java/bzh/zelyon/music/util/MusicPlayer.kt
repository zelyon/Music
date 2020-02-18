package bzh.zelyon.music.util

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AlertDialog
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.util.MusicPlayer.setOnCompletionListener
import java.io.File

object MusicPlayer: MediaPlayer() {

    var lastCurrentPosition = 0
    val listeners = mutableListOf<Listener>()
    var musics = mutableListOf<Music>()
    var currentMusic: Music? = null

    fun playMusics(musics: List<Music>) {
        this.musics = musics.toMutableList()
        currentMusic = musics[0]
        setOnCompletionListener { next() }
        playMusic()
    }

    fun addMusics(musics: List<Music>) {
        this.musics.addAll(musics)
    }

    private fun playMusic() {
        currentMusic?.let { currentMusic ->
            if (File(currentMusic.path).exists()) {
                reset()
                setDataSource(currentMusic.path)
                prepare()
                start()
            } else {
                next()
            }
        } ?: run {
            musics.clear()
            currentMusic = null
            setOnCompletionListener {}
            stop()
            reset()
        }
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
        playMusic()
    }

    fun previous() {
        val position = musics.indexOf(currentMusic)
        currentMusic = if (position > 0) musics[position - 1] else currentMusic
        playMusic()
    }

    fun next() {
        val position = musics.indexOf(currentMusic)
        currentMusic = if (position < musics.size-1) musics[position + 1] else null
        playMusic()
    }

    fun shuffle() {
        val position = musics.indexOf(currentMusic)
        musics.shuffle()
        currentMusic = musics[position]
        playMusic()
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