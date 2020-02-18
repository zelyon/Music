package bzh.zelyon.music.ui.view.fragment.bottom

import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.db.model.Playlist
import bzh.zelyon.music.ui.component.InputView
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsToolBarBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_musicplaylists.*
import kotlinx.android.synthetic.main.item_musicplaylist.view.*

class MusicPlaylistsFragment private constructor(): AbsToolBarBottomSheetFragment() {

    lateinit var music: Music

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        music = arguments?.getSerializable(ARG_MUSIC) as Music
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_musicplaylists_itemsview_playlists.apply {
            idLayoutItem = R.layout.item_musicplaylist
            idLayoutEmpty = R.layout.item_playlist_empty
            helper = PlaylistHelper()
        }
        loadPlayLists()
    }

    override fun getLayoutId() = R.layout.fragment_musicplaylists

    override fun getIdToolbar() = R.id.fragment_musicplaylists_toolbar

    override fun onIdClick(id: Int) {
        super.onIdClick(id)
        when (id) {
            R.id.fragment_musicplaylists_button_add -> {
                val input = InputView(absActivity).apply {
                    type = InputView.Type.TEXT
                    label = getString(R.string.fragment_musicplaylists_name)
                    mandatory = true
                }
                AlertDialog.Builder(absActivity).apply {
                    setView(input)
                    setPositiveButton(R.string.popup_ok, null)
                }.create().apply {
                    setOnShowListener {
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            if (input.checkValidity()) {
                                DB.getPlaylistDao().insert(Playlist(null, input.text.orEmpty()))
                                dismiss()
                                loadPlayLists()
                            }
                        }
                    }
                }.show()
            }
        }
    }

    private fun loadPlayLists() {
        fragment_musicplaylists_itemsview_playlists.items = DB.getPlaylistDao().getAll().toMutableList()
    }

    inner class PlaylistHelper: ItemsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val playlist = items[position]
            if (playlist is Playlist) {
                itemView.item_music_playlist_textview_name.text = playlist.name
                itemView.item_music_playlist_checkbox.isChecked = playlist.musics.any { it.id == music.id }
                itemView.item_music_playlist_checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        playlist.musics.add(music)
                    } else {
                        playlist.musics.remove(music)
                    }
                    DB.getPlaylistDao().update(playlist)
                }
            }
        }
        override fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {
            itemView.item_music_playlist_checkbox.isChecked = !itemView.item_music_playlist_checkbox.isChecked
        }
    }

    companion object {

        const val ARG_MUSIC = "ARG_MUSIC"

        fun getInstance(music: Music) = MusicPlaylistsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_MUSIC, music)
            }
        }
    }
}