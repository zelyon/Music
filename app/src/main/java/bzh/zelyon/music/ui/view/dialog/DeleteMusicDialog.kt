package bzh.zelyon.music.ui.view.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import java.io.File

class DeleteMusicDialog(context: Context, val music: Music) : AlertDialog.Builder(context) {

    init {
        setTitle(R.string.item_popup_delete_title)
        setMessage(context.getString(R.string.item_popup_delete_message, music.title))
        setPositiveButton(R.string.item_popup_delete_positive) { _, _ ->
            File(music.path).delete()
            // TODO nicolas_leveque 14/05/2020: reload all
        }
    }
}