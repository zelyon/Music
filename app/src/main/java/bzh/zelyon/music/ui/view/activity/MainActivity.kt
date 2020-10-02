package bzh.zelyon.music.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
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
import bzh.zelyon.music.ui.view.viewmodel.MainViewModel
import bzh.zelyon.music.util.MusicContent
import bzh.zelyon.music.util.MusicPlayer
import bzh.zelyon.music.util.MusicService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AbsActivity() {

    private val libraryFragment = LibraryFragment()
    private val playlistsFragment = PlaylistsFragment()
    private val playingFragment = PlayingFragment()

    private var currentFABState: MainViewModel.FABState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DB.init(this)
        startService(Intent(this, MusicService::class.java))

        val mainViewModel= ViewModelProvider(this).get(MainViewModel::class.java)

        MusicPlayer.mainViewModel = mainViewModel

        supportFragmentManager.addOnBackStackChangedListener {
            mainViewModel.currentFragment.value = getCurrentFragment()
        }

        activity_main_fab.setOnClickListener {
            if (getCurrentFragment() == playingFragment) {
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

        mainViewModel.currentFragment.observe(this) {
            activity_main_bottomnavigationview.animate()
                .translationY(if (it in listOf(libraryFragment, playlistsFragment)) 0F else activity_main_bottomnavigationview.height.toFloat() )
                .setDuration(400)
                .start()
        }

        mainViewModel.fabState.observe(this) {
            if (currentFABState != it) {
                currentFABState = it
                if (currentFABState == MainViewModel.FABState.HIDE) {
                    activity_main_fab.hide()
                } else {
                    activity_main_fab.show()
                    when (currentFABState) {
                        MainViewModel.FABState.ANIM_PLAY -> R.drawable.anim_playing
                        MainViewModel.FABState.ANIM_PAUSE -> R.drawable.anim_pause
                        MainViewModel.FABState.ICON_PLAY -> R.drawable.anim_play_to_pause
                        MainViewModel.FABState.ICON_PAUSE -> R.drawable.anim_pause_to_play
                        else -> null
                    }?.let { resId ->
                        val anim = AnimatedVectorDrawableCompat.create(this, resId)
                        activity_main_fab.setImageDrawable(anim)
                        anim?.start()
                    }
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