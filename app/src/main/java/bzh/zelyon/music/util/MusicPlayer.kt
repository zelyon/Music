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
    var musicPosition = -1

    fun playMusics(musics: List<Music>) {
        this.musics = musics.toMutableList()
        musicPosition = 0
        setOnCompletionListener { next() }
        run()
    }

    fun addMusics(musics: List<Music>) {
        this.musics.addAll(musics)
    }

    fun run() {
        if (musicPosition in musics.indices && File(musics[musicPosition].path).exists()) {
            reset()
            setDataSource(musics[musicPosition].path)
            prepare()
            start()
        } else {
            musics.clear()
            musicPosition = -1
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
        musicPosition = position
        run()
    }

    fun previous() {
        musicPosition--
        run()
    }

    fun next() {
        musicPosition++
        run()
    }

    fun shuffle() {
        musics.shuffle()
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