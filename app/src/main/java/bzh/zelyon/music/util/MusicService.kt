package bzh.zelyon.music.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.audiofx.AudioEffect
import android.os.Binder
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import bzh.zelyon.lib.extension.isNougat
import bzh.zelyon.music.R
import bzh.zelyon.music.ui.view.activity.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class MusicService: Service() {

    private var notifMode = NotifMode.BACKGROUND
    private var notificationManager: NotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        MusicPlayer.musicService = this
        val mediaButtonReceiverComponentName = ComponentName(applicationContext, MusicReceiver::class.java)
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            component = mediaButtonReceiverComponentName
        }, 0)
        mediaSession = MediaSessionCompat(this, "Music", mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent)
        mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                MusicPlayer.playOrPause()
            }
            override fun onPause() {
                MusicPlayer.playOrPause()
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
            override fun onMediaButtonEvent(mediaButtonEvent: Intent) = MusicReceiver.parseIntent(this@MusicService, mediaButtonEvent)
        })
        mediaSession?.setMediaButtonReceiver(mediaButtonReceiverPendingIntent)
        if (isNougat()) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = notificationManager?.getNotificationChannel(NOTIFICATION_CHANNEL_ID) ?: NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
                notificationChannel.description = getString(R.string.app_name)
                notificationChannel.enableLights(false)
                notificationChannel.enableVibration(false)
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
        mediaSession?.isActive = true
        sendBroadcast(Intent("bzh.zelyon.music.MUSIC_SERVICE_CREATED"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action?.toInt()) {
            KeyEvent.KEYCODE_MEDIA_STOP, KeyEvent.KEYCODE_MEDIA_CLOSE -> MusicPlayer.stop()
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> MusicPlayer.playOrPause()
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> MusicPlayer.previous()
            KeyEvent.KEYCODE_MEDIA_NEXT -> MusicPlayer.next()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        MusicPlayer.stop()
        mediaSession?.isActive = false
        mediaSession?.release()
        MusicPlayer.release()
        sendBroadcast(Intent("bzh.zelyon.music.MUSIC_SERVICE_DESTROYED"))
    }

    override fun onBind(intent: Intent) = MusicBinder()

    fun updateMetaDatasAndNotifs(delete: Boolean = false) {
        if (delete) {
            notificationManager?.cancel(NOTIFICATION_ID)
            stopForeground(true)
            sendBroadcast(Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicPlayer.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            })
            stopSelf()
        }
        sendBroadcast(Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicPlayer.audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        })
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(if (MusicPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED, MusicPlayer.currentPosition.toLong(), 1f)
                .build())

        MusicPlayer.playingMusic?.let { music ->
            if (isNougat()) {
                Glide.with(this)
                    .asBitmap()
                    .load(music)
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            showNotif(resource)
                        }
                        override fun onLoadStarted(placeholder: Drawable?) {
                            showNotif(BitmapFactory.decodeResource(resources, R.drawable.ic_music_notif))
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        private fun showNotif(bitmap: Bitmap) {
                            val builder = NotificationCompat.Builder(this@MusicService, NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setSubText(music.albumName)
                                .setLargeIcon(bitmap)
                                .setContentIntent(PendingIntent.getActivity(this@MusicService, 0, Intent(this@MusicService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }, 0))
                                .setDeleteIntent(PendingIntent.getService(this@MusicService, 0, Intent(KeyEvent.KEYCODE_MEDIA_CLOSE.toString()).apply {
                                    component = ComponentName(this@MusicService, MusicService::class.java)
                                }, 0))
                                .setContentTitle(music.title)
                                .setContentText(music.artistName)
                                .setOngoing(MusicPlayer.isPlaying)
                                .setShowWhen(false)
                                .addAction(NotificationCompat.Action(R.drawable.ic_previous_notif, getString(R.string.notif_previous), createPendindIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS.toString())))
                                .addAction(NotificationCompat.Action(if (MusicPlayer.isPlaying) R.drawable.ic_pause_notif else R.drawable.ic_play_notif, getString(R.string.notif_play_pause), createPendindIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE.toString())))
                                .addAction(NotificationCompat.Action(R.drawable.ic_next_notif, getString(R.string.notif_next), createPendindIntent(KeyEvent.KEYCODE_MEDIA_NEXT.toString())))
                                .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession?.sessionToken).setShowActionsInCompactView(0, 1, 2))
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                            val newNotifMode = if (MusicPlayer.isPlaying) NotifMode.FOREGROUND else NotifMode.BACKGROUND
                            if (notifMode != newNotifMode && newNotifMode == NotifMode.BACKGROUND) {
                                stopForeground(false)
                            }
                            if (newNotifMode == NotifMode.FOREGROUND) {
                                startForeground(NOTIFICATION_ID, builder.build())
                            } else if (newNotifMode == NotifMode.BACKGROUND) {
                                notificationManager?.notify(NOTIFICATION_ID, builder.build())
                            }
                            notifMode = newNotifMode
                        }
                    })
            }
            mediaSession?.setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.artistName)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, music.artistName)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, music.albumName)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.title)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, music.duration.toLong())
                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, MusicPlayer.playingPosition + 1.toLong())
                    .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, music.year.toLong())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, MusicPlayer.musics.size.toLong()).build())
        } ?: mediaSession?.setMetadata(null)
    }

    private fun createPendindIntent(action: String) = PendingIntent.getService(this, 0, Intent(action).apply {
        component = ComponentName(this@MusicService, MusicService::class.java)
    }, 0)

    inner class MusicBinder : Binder() {
        val service: MusicService get() = this@MusicService
    }

    enum class NotifMode {FOREGROUND, BACKGROUND}

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "music_notification"
    }
}