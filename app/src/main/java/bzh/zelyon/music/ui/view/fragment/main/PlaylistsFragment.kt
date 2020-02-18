package bzh.zelyon.music.ui.view.fragment.main

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.db.model.Playlist
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsFragment
import bzh.zelyon.music.ui.view.fragment.bottom.MusicsFragment
import bzh.zelyon.music.ui.view.fragment.edit.EditPlaylistFragment
import bzh.zelyon.music.utils.MusicManager
import bzh.zelyon.music.utils.dpToPx
import bzh.zelyon.music.utils.setImage
import kotlinx.android.synthetic.main.fragment_playlists.*
import kotlinx.android.synthetic.main.item_playlist.view.*

class PlaylistsFragment: AbsFragment(), MusicManager.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicManager.listeners.add(this)
    }

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

    override fun getLayoutId() = R.layout.fragment_playlists

    private fun loadPlayLists() {
        fragment_playlists_itemsview_playlists.items = DB.getPlaylistDao().getAll().toMutableList()
    }

    inner class PlaylistHelper: ItemsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val playlist = items[position]
            if (playlist is Playlist) {
                itemView.item_playlist_imageview_artwork.setImage(playlist, absActivity.getDrawable(R.drawable.ic_playlist))
                itemView.item_playlist_imageview_artwork.transitionName = playlist.getTransitionName()
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
                val artwork = (itemView.item_playlist_imageview_artwork.drawable as? BitmapDrawable)?.bitmap
                PopupMenu(absActivity, itemView.item_playlist_button_more).apply {
                    menuInflater.inflate(R.menu.item, menu)
                    menu.findItem(R.id.item_add).isVisible = MusicManager.isPlayingOrPause
                    menu.findItem(R.id.item_playlists).isVisible = false
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_play -> MusicManager.playMusics(playlist.musics)
                            R.id.item_add -> MusicManager.addMusics(playlist.musics)
                            R.id.item_edit_infos -> showFragment(EditPlaylistFragment.getInstance(playlist, artwork), transitionView = itemView.item_playlist_imageview_artwork)
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

    override fun onMusicFileDeleted(music: Music) {
        loadPlayLists()
    }
}