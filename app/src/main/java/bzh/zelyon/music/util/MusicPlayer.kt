package bzh.zelyon.music.util

import android.media.MediaPlayer
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.ui.view.viewmodel.MainViewModel
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

object MusicPlayer: MediaPlayer() {

    var mainViewModel: MainViewModel? = null
    private var lastCurrentPosition = 0
    val playingMusic get() = if (playingPosition in musics.indices && File(musics[playingPosition].path).exists()) musics[playingPosition] else null
    var playingPositions = mutableListOf<Int>()
    var musics = mutableListOf<Music>()
    var playingPosition = -1
    var isShuffle = false
    var isRepeat = false

    fun playMusics(musics: List<Music>) {
        this.musics = musics.toMutableList()
        playingPosition = 0
        playingPositions.add(playingPosition)
        run()
    }

    fun addMusics(musics: List<Music>) {
        this.musics.addAll(musics)
    }

    fun run() {
        val isPlaying = playingMusic?.let {
            reset()
            setDataSource(it.path)
            setOnCompletionListener { next(true) }
            prepare()
            start()
            true
        } ?: run {
            musics.clear()
            playingPosition = -1
            playingPositions.clear()
            setOnCompletionListener {}
            stop()
            reset()
            false
        }
        mainViewModel?.isPlaying?.value = isPlaying
        mainViewModel?.hasPlayingList?.value = isPlaying
    }

    fun pauseOrPlay() {
        mainViewModel?.isPlaying?.value = if (isPlaying) {
            lastCurrentPosition = currentPosition
            pause()
            false
        } else {
            seekTo(lastCurrentPosition)
            start()
            true
        }
    }

    fun goTo(current: Int) {
        lastCurrentPosition = current
        seekTo(current)
    }

    fun jumpTo(position: Int) {
        playingPosition = position
        playingPositions.add(playingPosition)
        run()
    }

    fun previous() {
        playingPositions = playingPositions.dropLast(1).toMutableList()
        playingPosition = playingPositions.last()
        run()
    }

    fun next(auto: Boolean = false) {
        when {
            isShuffle -> playingPosition = Random.nextInt(musics.indices)
            isRepeat && auto -> {}
            isRepeat && !auto -> playingPosition++
            else -> playingPosition++
        }
        playingPositions.add(playingPosition)
        run()
    }
}