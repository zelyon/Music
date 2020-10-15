package bzh.zelyon.music.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.core.content.ContextCompat

class MusicReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (parseIntent(context, intent) && isOrderedBroadcast) {
            abortBroadcast()
        }
    }

    companion object {
        fun parseIntent(context: Context, intent: Intent): Boolean {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
            if (Intent.ACTION_MEDIA_BUTTON == intent.action && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val intentMusicService = Intent(context, MusicService::class.java).setAction(event.keyCode.toString())
                try {
                    context.startService(intentMusicService)
                } catch (ignored: IllegalStateException) {
                    ContextCompat.startForegroundService(context, intentMusicService)
                }
                return true
            }
            return false
        }
    }
}