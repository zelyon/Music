package bzh.zelyon.music.ui.view.fragment.bottom

import android.app.AlertDialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.db.model.Playlist
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsToolBarBottomSheetFragment
import bzh.zelyon.music.utils.vibrate
import kotlinx.android.synthetic.main.dialog_playlist.view.*
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

        (fragment_musicplaylists_itemsview_playlists as ItemsView<Playlist>).apply {
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
                val dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_playlist, null, false)
                AlertDialog.Builder(absActivity).apply {
                    setTitle(R.string.fragment_musicplaylists_popup_title)
                    setView(dialogLayout)
                    setPositiveButton(R.string.fragment_musicplaylists_popup_positive, null)
                }.create().apply {
                    setOnShowListener {
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val name = dialogLayout.dialog_playlist_edittext_name.text.toString()
                            if (name.isNotBlank()) {
                                DB.getPlaylistDao().insert(Playlist(null, name))
                                dismiss()
                                loadPlayLists()
                            } else {
                                dialogLayout.dialog_playlist_editlayout_name.apply {
                                    error = getString(R.string.fragment_musicplaylists_popup_error)
                                    vibrate()
                                }
                            }
                        }
                    }
                }.show()
            }
        }
    }

    private fun loadPlayLists() {
        (fragment_musicplaylists_itemsview_playlists as ItemsView<Playlist>).items = DB.getPlaylistDao().getAll().toMutableList()
    }

    inner class PlaylistHelper: ItemsView.Helper<Playlist>() {
        override fun onBindItem(itemView: View, items: List<Playlist>, position: Int) {
            val playlist = items[position]
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
        override fun onItemClick(itemView: View, items: List<Playlist>, position: Int) {
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