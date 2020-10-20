package bzh.zelyon.music.ui.view

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.multidex.MultiDexApplication
import bzh.zelyon.lib.extension.isMarshmallow
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.ui.view.activity.ShortcutActivity

class MainApplication: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        DB.init(this)

        if (isMarshmallow()) {
            ShortcutManagerCompat.removeAllDynamicShortcuts(this)
            ShortcutManagerCompat.addDynamicShortcuts(this, listOf(
                ShortcutInfoCompat.Builder(this, ShortcutActivity.SHORTCUT_SHUFFLE)
                    .setShortLabel(getString(R.string.fragment_library_menu_shuffle))
                    .setLongLabel(getString(R.string.fragment_library_menu_shuffle))
                    .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shuffle_shortcut))
                    .setIntent(Intent(this, ShortcutActivity::class.java).setAction(ACTION_VIEW))
                    .build()
            ))
        }
    }
}