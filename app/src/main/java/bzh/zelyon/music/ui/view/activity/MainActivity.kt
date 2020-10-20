package bzh.zelyon.music.ui.view.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import bzh.zelyon.lib.extension.fullBack
import bzh.zelyon.lib.extension.getCurrentFragment
import bzh.zelyon.lib.extension.showFragment
import bzh.zelyon.lib.ui.view.activity.AbsActivity
import bzh.zelyon.lib.ui.view.fragment.AbsFragment
import bzh.zelyon.music.R
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

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicService.MusicBinder) {
                service.service.updateBroadcastMetadatasNotifs()
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val musicServiceIntent = Intent(this, MusicService::class.java)
        startService(musicServiceIntent)
        bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        ViewCompat.setOnApplyWindowInsetsListener(activity_main_root) { _, insets ->
            insets.consumeSystemWindowInsets()
        }

        val mainViewModel= ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.currentFragment.observe(this) {
            updateNavigationBar(it)
        }
        mainViewModel.fabState.observe(this) {
            updateFABState(it)
        }
        MusicPlayer.mainViewModel = mainViewModel

        supportFragmentManager.addOnBackStackChangedListener {
            mainViewModel.currentFragment.value = getCurrentFragment()
        }

        activity_main_fab.setOnClickListener {
            if (getCurrentFragment() == playingFragment) {
                MusicPlayer.playOrPause()
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

        intent?.let {
            manageIntent(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            manageIntent(it)
        }
    }

    override fun getLayoutId() = R.layout.activity_main

    override fun getFragmentContainerId() = R.id.activity_main_container

    private fun manageIntent(intent: Intent) {
        intent.data?.let { uri ->
            MusicContent.getMusicsFromUri(this, uri)?.let { musics ->
                MusicPlayer.playMusics(musics)
            }
        }
    }

    private fun updateNavigationBar(fragment: AbsFragment?) {
        activity_main_bottomnavigationview.animate()
            .translationY(if (fragment in listOf(libraryFragment, playlistsFragment)) 0F else activity_main_bottomnavigationview.height.toFloat())
            .setDuration(400)
            .start()
    }

    private fun updateFABState(fabState: MainViewModel.FABState) {
        if (currentFABState != fabState) {
            currentFABState = fabState
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