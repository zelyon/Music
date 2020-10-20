package bzh.zelyon.music.ui.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bzh.zelyon.music.util.MusicService

class ShortcutActivity : AppCompatActivity() {

    companion object {
        const val SHORTCUT_SHUFFLE = "SHORTCUT_SHUFFLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, MusicService::class.java).setAction(SHORTCUT_SHUFFLE))
        finish()
    }
}