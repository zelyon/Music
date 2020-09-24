package bzh.zelyon.music.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import bzh.zelyon.lib.extension.fullBack
import bzh.zelyon.lib.extension.getCurrentFragment
import bzh.zelyon.lib.extension.showFragment
import bzh.zelyon.lib.ui.view.activity.AbsActivity
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
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

    private var fabState: FABState? = null

    private enum class FABState { ANIM_PLAY, ANIM_PAUSE, ICON_PLAY, ICON_PAUSE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        DB.init(this)
        startService(Intent(this, MusicService::class.java))

        activity_main_fab.setOnClickListener {
            if (getCurrentFragment() is PlayingFragment) {
                MusicPlayer.pauseOrPlay()
            } else {
                showFragment(playingFragment)
            }
        }

        activity_main_bottomnavigationview.setOnNavigationItemSelectedListener {
            fullBack()
            when (it.itemId) {
                R.id.activity_main_library -> libraryFragment
                R.id.activity_main_playlists -> playlistsFragment
                else -> null
            }?.let { fragment ->
                showFragment(fragment, false)
            }
            true
        }

        activity_main_bottomnavigationview.selectedItemId = R.id.activity_main_library

        fixedRateTimer(period = DURATION) {
            runOnUiThread {

                activity_main_bottomnavigationview.animate()
                    .translationY(if (getCurrentFragment() in listOf(libraryFragment, playlistsFragment)) 0F else activity_main_bottomnavigationview.height.toFloat() )
                    .setDuration(DURATION)
                    .start()

                if (MusicPlayer.playingMusic != null) {

                    activity_main_fab.show()

                    when {
                        getCurrentFragment() is PlayingFragment && MusicPlayer.isPlaying -> FABState.ICON_PLAY
                        getCurrentFragment() is PlayingFragment && !MusicPlayer.isPlaying -> FABState.ICON_PAUSE
                        getCurrentFragment() !is PlayingFragment && MusicPlayer.isPlaying -> FABState.ANIM_PLAY
                        getCurrentFragment() !is PlayingFragment && !MusicPlayer.isPlaying -> FABState.ANIM_PAUSE
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
            MusicContent.getMusicsFromUri(this, uri)?.let { musics ->
                MusicPlayer.playMusics(musics)
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_main

    override fun getFragmentContainerId() = R.id.activity_main_container
}