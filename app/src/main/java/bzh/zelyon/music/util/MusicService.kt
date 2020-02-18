package bzh.zelyon.music.util

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.media.MediaBrowserServiceCompat
import bzh.zelyon.music.R

class MusicService: MediaBrowserServiceCompat() {

    override fun onCreate() {
        super.onCreate()

        val componentName = ComponentName(applicationContext, MusicReceiver::class.java)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).setComponent(componentName)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
        val mediaSessionCompat = MediaSessionCompat(this, getString(R.string.app_name), componentName, pendingIntent).apply {
            setMediaButtonReceiver(pendingIntent)
            setCallback(MediaSessionCallback(applicationContext))
            isActive = true
        }
        sessionToken = mediaSessionCompat.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action?.toInt()) {
            KeyEvent.KEYCODE_MEDIA_STOP -> MusicPlayer.stop()
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> MusicPlayer.pauseOrPlay()
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> MusicPlayer.previous()
            KeyEvent.KEYCODE_MEDIA_NEXT -> MusicPlayer.next()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?) = BrowserRoot(getString(
                R.string.app_name
            ), null)

    private class MediaSessionCallback(val context: Context): MediaSessionCompat.Callback() {
        override fun onPlay() {
            MusicPlayer.pauseOrPlay()
        }
        override fun onPause() {
            MusicPlayer.pauseOrPlay()
        }
        override fun onSkipToNext() {
            MusicPlayer.next()
        }
        override fun onSkipToPrevious() {
            MusicPlayer.previous()
        }
        override fun onStop() {
            MusicPlayer.stop()
        }
        override fun onSeekTo(pos: Long) {
            MusicPlayer.goTo(pos.toInt())
        }
        override fun onMediaButtonEvent(mediaButtonEvent: Intent) = MusicReceiver.parseIntent(context, mediaButtonEvent)
    }
}