package bzh.zelyon.music.ui.view.fragment.edit

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Playlist
import kotlinx.android.synthetic.main.fragment_edit_playlist.*

class EditPlaylistFragment private constructor(): AbsEditFragment<Playlist>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_edit_playlist_inputview_name.text = absModel.name
    }

    override fun getFormLayoutId() = R.layout.fragment_edit_playlist

    override fun onClickArtwork() {}

    override fun onSave() {
        absModel.name = fragment_edit_playlist_inputview_name.text.orEmpty()
        DB.getPlaylistDao().update(absModel)
        back()
    }

    companion object {
        fun getInstance(playlist: Playlist, artwork: Bitmap?) = EditPlaylistFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_ABS_MODEL, playlist)
                putParcelable(ARG_ARTORK, artwork)
            }
        }
    }
}