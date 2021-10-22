package bzh.zelyon.music.ui.view

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import bzh.zelyon.lib.ui.view.AbsApplication
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.ui.view.activity.ShortcutActivity

class MainApplication: AbsApplication() {

    override fun onCreate() {
        super.onCreate()

        DB.init(this)
    }

    override fun initDynmicShortcut() {
        super.initDynmicShortcut()
        ShortcutManagerCompat.addDynamicShortcuts(this, listOf(
            ShortcutInfoCompat.Builder(this, ShortcutActivity.SHORTCUT_SHUFFLE)
                .setShortLabel(getString(R.string.shortcut_shuffle))
                .setLongLabel(getString(R.string.shortcut_shuffle))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_shuffle))
                .setIntent(Intent(this, ShortcutActivity::class.java)
                    .putExtra(ShortcutActivity.SHORTCUT, ShortcutActivity.SHORTCUT_SHUFFLE)
                    .setAction(ACTION_VIEW))
                .build(),
            ShortcutInfoCompat.Builder(this, ShortcutActivity.SHORTCUT_LAST)
                .setShortLabel(getString(R.string.shortcut_last))
                .setLongLabel(getString(R.string.shortcut_last))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_last))
                .setIntent(Intent(this, ShortcutActivity::class.java)
                    .putExtra(ShortcutActivity.SHORTCUT, ShortcutActivity.SHORTCUT_LAST)
                    .setAction(ACTION_VIEW))
                .build()
        ))
    }
}