package bzh.zelyon.music.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent

class MusicReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (parseIntent(context, intent) && isOrderedBroadcast) {
            abortBroadcast()
        }
    }

    companion object {
        fun parseIntent(context: Context, intent: Intent): Boolean {
            intent.getParcelableExtra<KeyEvent?>(Intent.EXTRA_KEY_EVENT)?.let { event ->
                if (intent.action == Intent.ACTION_MEDIA_BUTTON && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    context.startService(Intent(context, MusicService::class.java).setAction(event.keyCode.toString()))
                    return true
                }
            }
            return false
        }
    }
}