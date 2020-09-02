package bzh.zelyon.music.ui.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.common.ui.view.activity.AbsActivity
import bzh.zelyon.music.ui.view.fragment.main.LibraryFragment
import bzh.zelyon.music.ui.view.fragment.main.PlayingFragment
import bzh.zelyon.music.ui.view.fragment.main.PlaylistsFragment
import bzh.zelyon.music.util.MusicContent
import bzh.zelyon.music.util.MusicPlayer
import bzh.zelyon.music.util.MusicService
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.fixedRateTimer

class MainActivity : AbsActivity() {

    companion object {
        const val DURATION = 400L
    }

    private val libraryFragment = LibraryFragment()
    private val playlistsFragment = PlaylistsFragment()
    private val playingFragment = PlayingFragment()

    private val playingFragmentIsVisible get() = getCurrentFragment() is PlayingFragment
    private var fabState: FABState? = null

    private enum class FABState { ANIM_PLAY, ANIM_PAUSE, ICON_PLAY, ICON_PAUSE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DB.init(this)
        startService(Intent(this, MusicService::class.java))

        activity_main_fab.setOnClickListener {
            if (playingFragmentIsVisible) {
                MusicPlayer.pauseOrPlay()
            } else {
                showFragment(playingFragment)
            }
        }

        activity_main_bottomnavigationview.setOnNavigationItemSelectedListener {
            fullBack()
            showFragment(when (it.itemId) {
                R.id.activity_main_library -> libraryFragment
                R.id.activity_main_playlists -> playlistsFragment
                else -> Fragment()
            }, false)
            true
        }

        activity_main_bottomnavigationview.selectedItemId = R.id.activity_main_library

        fixedRateTimer(period = DURATION) {
            runOnUiThread {

                activity_main_bottomnavigationview.animate()
                    .translationY(if (playingFragmentIsVisible) activity_main_bottomnavigationview.height.toFloat() else 0F)
                    .setDuration(DURATION)
                    .start()

                if (MusicPlayer.playingMusic != null) {

                    activity_main_fab.show()

                    when {
                        playingFragmentIsVisible && MusicPlayer.isPlaying -> FABState.ICON_PLAY
                        playingFragmentIsVisible && !MusicPlayer.isPlaying -> FABState.ICON_PAUSE
                        !playingFragmentIsVisible && MusicPlayer.isPlaying -> FABState.ANIM_PLAY
                        !playingFragmentIsVisible && !MusicPlayer.isPlaying -> FABState.ANIM_PAUSE
                        else -> null
                    }?.let {
                        if (it != fabState) {
                            val anim = AnimatedVectorDrawableCompat.create(
                                baseContext, when (it) {
                                    FABState.ICON_PLAY -> R.drawable.anim_play_to_pause
                                    FABState.ICON_PAUSE -> R.drawable.anim_pause_to_play
                                    FABState.ANIM_PLAY -> R.drawable.anim_playing
                                    FABState.ANIM_PAUSE -> R.drawable.anim_pause
                                }
                            )
                            activity_main_fab.setImageDrawable(anim)
                            anim?.start()
                            fabState = it
                        }
                    }
                } else {
                    activity_main_fab.hide()
                }
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        super.handleIntent(intent)
        intent.data?.let { uri ->
            MusicContent.getMusicFromUri(this, uri)?.let { music ->
                MusicPlayer.playMusics(listOf(music))
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_main

    override fun getFragmentContainerId() = R.id.activity_main_container
}