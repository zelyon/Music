package bzh.zelyon.music.ui.view.fragment.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Playlist
import bzh.zelyon.music.extension.dpToPx
import bzh.zelyon.music.ui.component.InputView
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsFragment
import bzh.zelyon.music.ui.view.fragment.bottom.MusicsFragment
import bzh.zelyon.music.util.MusicPlayer
import kotlinx.android.synthetic.main.fragment_playlists.*
import kotlinx.android.synthetic.main.item_playlist.view.*
import java.io.File

class PlaylistsFragment: AbsFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_playlists_itemsview_playlists.apply {
            nbColumns = 2
            spaceDivider = absActivity.dpToPx(8).toInt()
            idLayoutItem = R.layout.item_playlist
            idLayoutEmpty = R.layout.item_playlist_empty
            isFastScroll = true
            helper = PlaylistHelper()
        }

        loadPlayLists()
    }

    override fun getIdLayout() = R.layout.fragment_playlists

    private fun loadPlayLists() {
        val playlists = DB.getPlaylistDao().getAll().toMutableList()
        playlists.forEach { playlist ->
            if (playlist.musics.isEmpty()) {
                playlists.remove(playlist)
            } else {
                playlist.musics.forEach { music ->
                    if (!File(music.path).exists()) {
                        playlist.musics.remove(music)
                        DB.getPlaylistDao().update(playlist)
                    }
                }
            }
        }
        fragment_playlists_itemsview_playlists.items = playlists
    }

    inner class PlaylistHelper: ItemsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val playlist = items[position]
            if (playlist is Playlist) {
                itemView.item_playlist_textview_name.text = playlist.name
                itemView.item_playlist_textview_nbmusic.text = resources.getQuantityString(R.plurals.item_music_nb, playlist.musics.size, playlist.musics.size)
                itemView.item_playlist_button_more.setOnClickListener { onItemLongClick(itemView, items, position) }
            }
        }
        override fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {
            val playlist = items[position]
            if (playlist is Playlist) {
                if (playlist.musics.isNotEmpty()) {
                    showFragment(MusicsFragment.getInstance(playlist.name, playlist.musics))
                }
            }
        }
        override fun onItemLongClick(itemView: View, items: MutableList<*>, position: Int) {
            val playlist = items[position]
            if (playlist is Playlist) {
                PopupMenu(absActivity, itemView.item_playlist_button_more).apply {
                    menuInflater.inflate(R.menu.item, menu)
                    menu.findItem(R.id.item_add).isVisible = MusicPlayer.playingMusic != null
                    menu.findItem(R.id.item_playlists).isVisible = false
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_play -> MusicPlayer.playMusics(playlist.musics)
                            R.id.item_add -> MusicPlayer.addMusics(playlist.musics)
                            R.id.item_edit -> {
                                val input = InputView(absActivity).apply {
                                    type = InputView.Type.TEXT
                                    label = getString(R.string.fragment_playlists_name)
                                    mandatory = true
                                }
                                AlertDialog.Builder(absActivity).apply {
                                    setTitle(R.string.fragment_playlists_rename)
                                    setView(input)
                                    setPositiveButton(R.string.popup_ok, null)
                                }.create().apply {
                                    setOnShowListener {
                                        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                                            if (input.checkValidity()) {
                                                playlist.name = input.text.orEmpty()
                                                DB.getPlaylistDao().update(playlist)
                                                dismiss()
                                                loadPlayLists()
                                            }
                                        }
                                    }
                                }.show()
                            }
                            R.id.item_delete -> {
                                DB.getPlaylistDao().delete(playlist)
                                loadPlayLists()
                            }
                        }
                        return@setOnMenuItemClickListener true
                    }
                }.show()
            }
        }
    }
}