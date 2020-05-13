package bzh.zelyon.music.util

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AlertDialog
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.util.MusicPlayer.setOnCompletionListener
import java.io.File
import kotlin.random.Random

object MusicPlayer: MediaPlayer() {

    private var lastCurrentPosition = 0
    val listeners = mutableListOf<Listener>()
    val playingMusic get() = if (playingPosition in musics.indices && File(musics[playingPosition].path).exists()) musics[playingPosition] else null
    var musics = mutableListOf<Music>()
    var playingPosition = -1
    var isShuffle = false
    var isRepeat = false

    fun playMusics(musics: List<Music>) {
        this.musics = musics.toMutableList()
        playingPosition = 0
        run()
    }

    fun addMusics(musics: List<Music>) {
        this.musics.addAll(musics)
    }

    fun run() {
        playingMusic?.let {
            reset()
            setDataSource(it.path)
            setOnCompletionListener { next() }
            prepare()
            start()
        } ?: run {
            musics.clear()
            playingPosition = -1
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

    fun jumpTo(position: Int) {
        playingPosition = position
        run()
    }

    fun previous() {
        when {
            isRepeat -> {}
            isShuffle -> playingPosition = Random.nextInt(0, musics.size - 1)
            else -> playingPosition--
        }
        run()
    }

    fun next() {
        when {
            isRepeat -> {}
            isShuffle -> playingPosition = Random.nextInt(0, musics.size - 1)
            else -> playingPosition++
        }
        run()
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