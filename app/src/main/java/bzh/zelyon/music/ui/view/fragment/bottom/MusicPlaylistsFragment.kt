package bzh.zelyon.music.ui.view.fragment.bottom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import bzh.zelyon.lib.ui.component.CollectionsView
import bzh.zelyon.lib.ui.component.InputView
import bzh.zelyon.lib.ui.component.Popup
import bzh.zelyon.lib.ui.view.fragment.AbsToolBarBottomSheetFragment
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.db.model.Playlist
import bzh.zelyon.music.ui.view.viewmodel.PlaylistViewModel
import kotlinx.android.synthetic.main.fragment_musicplaylists.*
import kotlinx.android.synthetic.main.item_musicplaylist.view.*

class MusicPlaylistsFragment private constructor(): AbsToolBarBottomSheetFragment() {

    lateinit var music: Music

    private val playlistViewModel: PlaylistViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        music = arguments?.getSerializable(ARG_MUSIC) as Music
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_musicplaylists_collectionview_playlists.helper = PlaylistHelper()

        playlistViewModel.playlists.observe(viewLifecycleOwner) {
            fragment_musicplaylists_collectionview_playlists.items = it.toMutableList()
        }
    }

    override fun getIdLayout() = R.layout.fragment_musicplaylists

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
                Popup(absActivity,
                    title = getString(R.string.fragment_musicplaylists_add),
                    customView = input,
                    positiveText = getString(R.string.popup_ok),
                    positiveDismiss = false,
                    positiveClick = {
                        if (input.checkValidity()) {
                            DB.getPlaylistDao().insert(Playlist(null, input.text))
                            Popup.dismiss()
                        }
                    }).show()
            }
        }
    }

    inner class PlaylistHelper: CollectionsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val playlist = items[position]
            if (playlist is Playlist) {
                itemView.item_music_playlist_textview_name.text = playlist.name
                itemView.item_music_playlist_checkbox.setOnCheckedChangeListener(null)
                itemView.item_music_playlist_checkbox.isChecked = playlist.musics.any { it.id == music.id }
                itemView.item_music_playlist_checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        playlist.musics.add(music)
                    } else {
                        playlist.musics.remove(music)
                    }
                    DB.getPlaylistDao().update(playlist)
                }
                itemView.item_music_playlist_button_more.setOnClickListener {
                    val choices = mutableListOf<Popup.Choice>()
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