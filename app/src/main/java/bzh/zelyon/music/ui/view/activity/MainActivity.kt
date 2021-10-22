package bzh.zelyon.music.ui.view.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import bzh.zelyon.lib.extension.ActionFragment
import bzh.zelyon.lib.extension.actionFragment
import bzh.zelyon.lib.extension.getCurrentFragment
import bzh.zelyon.lib.ui.view.activity.AbsBottomNavigationActivity
import bzh.zelyon.lib.ui.view.fragment.AbsFragment
import bzh.zelyon.lib.util.Launch
import bzh.zelyon.music.R
import bzh.zelyon.music.ui.view.fragment.main.LibraryFragment
import bzh.zelyon.music.ui.view.fragment.main.PlayingFragment
import bzh.zelyon.music.ui.view.fragment.main.PlaylistsFragment
import bzh.zelyon.music.ui.view.viewmodel.MainViewModel
import bzh.zelyon.music.util.MusicContent
import bzh.zelyon.music.util.MusicPlayer
import bzh.zelyon.music.util.MusicService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AbsBottomNavigationActivity() {

    private val libraryFragment = LibraryFragment()
    private val playlistsFragment = PlaylistsFragment()
    private val playingFragment = PlayingFragment()

    private var currentFABState: MainViewModel.FABState? = null

    private val mainViewModel: MainViewModel by viewModels()

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

        MusicPlayer.mainViewModel = mainViewModel
        mainViewModel.currentFragment.observe(this) {
            updateNavigationBar(it)
        }
        mainViewModel.fabState.observe(this) {
            updateFABState(it)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            mainViewModel.currentFragment.value = getCurrentFragment()
        }

        mainViewModel.isPlaying.value = MusicPlayer.isPlaying
        mainViewModel.hasPlayingList.value = MusicPlayer.isPlaying

        activity_main_fab.setOnClickListener {
            if (getCurrentFragment() == playingFragment) {
                MusicPlayer.playOrPause()
            } else {
                actionFragment(playingFragment, ActionFragment.Add, animFromBottom = true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    override fun getLayoutId() = R.layout.activity_main

    override fun getFragmentContainerId() = R.id.activity_main_container

    override fun handleIntent(intent: Intent) {
        super.handleIntent(intent)
        intent.data?.let { uri ->
            MusicContent.getMusicsFromUri(this, uri)?.let { musics ->
                MusicPlayer.playMusics(musics)
            }
        }
    }

    override fun getBottomNavigationView(): BottomNavigationView = activity_main_bottomnavigationview

    override fun getSelectedNavigationItemId() = R.id.activity_main_library

    override fun getNavigationMenuId() = R.menu.activity_main

    override fun getNavigationItems() = listOf(NavigationItem(R.id.activity_main_library, libraryFragment), NavigationItem(R.id.activity_main_playlists, playlistsFragment))

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

    fun launchFilesPermission(applicationId: String, result:(Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                result.invoke(true)
            } else {
                launchWithResult(Launch.Simple(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$applicationId"))) { _, _ ->
                    result.invoke(Environment.isExternalStorageManager())
                })
            }
        } else {
            launchWithResult(Launch.Permission(mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                result.invoke(it)
            })
        }
    }
}