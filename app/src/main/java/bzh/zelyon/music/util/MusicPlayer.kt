package bzh.zelyon.music.util

import android.media.MediaPlayer
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.ui.view.viewmodel.MainViewModel
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

object MusicPlayer: MediaPlayer() {

    var musicService: MusicService? = null
    var mainViewModel: MainViewModel? = null
    private var lastCurrentPosition = 0
    val playingMusic get() = if (playingPosition in musics.indices && File(musics[playingPosition].path).exists()) musics[playingPosition] else null
    var playingPositions = mutableListOf<Int>()
    var musics = mutableListOf<Music>()
    var playingPosition = -1
    var isShuffle = false
    var isRepeat = false

    override fun stop() {
        super.stop()
        musicService?.updateMetaDatasAndNotifs(true)
    }

    fun playMusics(musics: List<Music>) {
        this.musics = musics.toMutableList()
        playingPosition = 0
        playingPositions.add(playingPosition)
        run()
    }

    fun addMusics(musics: List<Music>) {
        this.musics.addAll(musics)
        musicService?.updateMetaDatasAndNotifs()
    }

    fun run() {
        val isPlaying = playingMusic?.let {
            reset()
            setDataSource(it.path)
            setOnCompletionListener { next() }
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
        musicService?.updateMetaDatasAndNotifs()
    }

    fun playOrPause() {
        mainViewModel?.isPlaying?.value = if (isPlaying) {
            lastCurrentPosition = currentPosition
            pause()
            false
        } else {
            seekTo(lastCurrentPosition)
            start()
            true
        }
        musicService?.updateMetaDatasAndNotifs()
    }

    fun goTo(current: Int) {
        lastCurrentPosition = current
        seekTo(current)
        musicService?.updateMetaDatasAndNotifs()
    }

    fun jumpTo(position: Int) {
        playingPosition = position
        playingPositions.add(playingPosition)
        run()
    }

    fun previous() {
        if (playingPositions.size > 1) {
            playingPositions = playingPositions.dropLast(1).toMutableList()
        }
        playingPosition = playingPositions.last()
        run()
    }

    fun next() {
        playingPosition = when {
            isRepeat -> playingPosition
            isShuffle -> Random.nextInt(musics.indices)
            else -> playingPosition+1
        }
        playingPositions.add(playingPosition)
        run()
    }
}