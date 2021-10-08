package bzh.zelyon.music.ui.view.fragment.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import bzh.zelyon.lib.extension.getStatusBarHeight
import bzh.zelyon.lib.ui.component.CollectionsView
import bzh.zelyon.lib.ui.component.InputView
import bzh.zelyon.lib.ui.component.Popup
import bzh.zelyon.lib.ui.view.fragment.AbsFragment
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Playlist
import bzh.zelyon.music.ui.view.fragment.bottom.MusicsFragment
import bzh.zelyon.music.ui.view.viewmodel.PlaylistViewModel
import bzh.zelyon.music.util.MusicPlayer
import kotlinx.android.synthetic.main.fragment_playlists.*
import kotlinx.android.synthetic.main.item_playlist.view.*

class PlaylistsFragment: AbsFragment() {

    private val playlistViewModel: PlaylistViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_playlists_collectionview_playlists.headerHeight = absActivity.getStatusBarHeight().toFloat()
        fragment_playlists_collectionview_playlists.helper = PlaylistHelper()

        playlistViewModel.playlistsNotEmpty.observe(viewLifecycleOwner) {
            fragment_playlists_collectionview_playlists.items = it.toMutableList()
        }
    }

    override fun getIdLayout() = R.layout.fragment_playlists

    inner class PlaylistHelper: CollectionsView.Helper() {
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
                showFragment(MusicsFragment.getInstance(playlist.name, playlist.musics))
            }
        }
        override fun onItemLongClick(itemView: View, items: MutableList<*>, position: Int) {
            val playlist = items[position]
            if (playlist is Playlist) {
                val choices = mutableListOf<Popup.Choice>()
                choices.add(Popup.Choice(getString(R.string.popup_play)) {
                    MusicPlayer.playMusics(playlist.musics)
                })
                if (MusicPlayer.playingMusic != null) {
                    choices.add(Popup.Choice(getString(R.string.popup_add)) {
                        MusicPlayer.addMusics(playlist.musics)
                    })
                }
                choices.add(Popup.Choice(getString(R.string.popup_rename)) {
                    val input = InputView(absActivity).apply {
                        type = InputView.Type.TEXT
                        label = getString(R.string.popup_name)
                        mandatory = true
                        text = playlist.name
                    }
                    Popup(absActivity,
                        title = getString(R.string.popup_rename),
                        customView = input,
                        positiveText = getString(R.string.popup_ok),
                        positiveDismiss = false,
                        positiveClick = {
                            if (input.checkValidity()) {
                                playlist.name = input.text
                                DB.getPlaylistDao().update(playlist)
                                Popup.dismiss()
                            }
                        }).show()
                })
                choices.add(Popup.Choice(getString(R.string.popup_delete)) {
                    DB.getPlaylistDao().delete(playlist)
                })
                Popup(absActivity, choices = choices).showBottom()
            }
        }
    }
}