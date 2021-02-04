package bzh.zelyon.music.ui.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bzh.zelyon.music.util.MusicService

class ShortcutActivity : AppCompatActivity() {

    companion object {
        const val SHORTCUT = "SHORTCUT"
        const val SHORTCUT_SHUFFLE = "SHORTCUT_SHUFFLE"
        const val SHORTCUT_LAST = "SHORTCUT_LAST"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, MusicService::class.java).setAction(intent.getStringExtra(SHORTCUT)))
        finish()
    }
}