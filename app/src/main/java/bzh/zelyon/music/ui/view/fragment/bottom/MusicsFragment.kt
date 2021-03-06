package bzh.zelyon.music.ui.view.fragment.bottom

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.lifecycle.ViewModelProvider
import bzh.zelyon.lib.extension.drawableResToDrawable
import bzh.zelyon.lib.extension.setImage
import bzh.zelyon.lib.ui.component.CollectionsView
import bzh.zelyon.lib.ui.component.Popup
import bzh.zelyon.lib.ui.view.fragment.AbsToolBarBottomSheetFragment
import bzh.zelyon.music.BuildConfig
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.ui.view.fragment.edit.EditMusicFragment
import bzh.zelyon.music.ui.view.viewmodel.LibraryViewModel
import bzh.zelyon.music.util.MusicPlayer
import kotlinx.android.synthetic.main.fragment_musics.*
import kotlinx.android.synthetic.main.item_music.view.*
import java.io.File

class MusicsFragment private constructor(): AbsToolBarBottomSheetFragment() {

    lateinit var musics: List<Music>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musics = (arguments?.getSerializable(ARG_MUSICS) as List<*>).map { it as Music }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_musics_collectionview_musics.helper = MusicHelper()
        fragment_musics_collectionview_musics.items = musics.toMutableList()
    }

    override fun getIdToolbar() = R.id.fragment_musics_toolbar

    override fun getLayoutId() = R.layout.fragment_musics

    override fun getTitleToolBar() = arguments?.getString(ARG_TITLE).orEmpty()

    inner class MusicHelper: CollectionsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                itemView.item_music_imageview_artwork.setImage(music, absActivity.drawableResToDrawable(R.drawable.ic_music_item))
                itemView.item_music_imageview_artwork.transitionName = music.getTransitionName()
                itemView.item_music_textview_title.text = music.title
                itemView.item_music_textview_infos.text = music.getInfos(
                    title = false,
                    artist = false,
                    album = true,
                    duration = true
                )
                itemView.item_music_imagebutton.setImageResource(R.drawable.ic_more)
                itemView.item_music_imagebutton.setOnClickListener { onItemLongClick(itemView, items, position) }
            }
        }
        override fun onItemClick(itemView: View, items: MutableList<*>, position: Int)  {
            back()
            MusicPlayer.playMusics(listOf(items[position] as Music))
        }
        override fun onItemLongClick(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                val artwork = (itemView.item_music_imageview_artwork.drawable as? BitmapDrawable)?.bitmap
                val choices = mutableListOf<Popup.Choice>()
                choices.add(Popup.Choice(getString(R.string.popup_play)) {
                    back()
                    MusicPlayer.playMusics(listOf(music))
                })
                if (MusicPlayer.playingMusic != null) {
                    choices.add(Popup.Choice(getString(R.string.popup_add)) {
                        back()
                        MusicPlayer.addMusics(listOf(music))
                    })
                }
                choices.add(Popup.Choice(getString(R.string.popup_edit)) {
                    showFragment(EditMusicFragment.getInstance(music, artwork), transitionView = itemView.item_music_imageview_artwork)
                    back()
                })
                choices.add(Popup.Choice(getString(R.string.popup_delete)) {
                    Popup(absActivity,
                        title = getString(R.string.popup_delete),
                        message = getString(R.string.popup_delete_message, music.title),
                        positiveText = getString(R.string.popup_yes),
                        positiveClick = {
                            if (File(music.path).delete()) {
                                back()
                                ViewModelProvider(absActivity).get(LibraryViewModel::class.java).needReloadLibrary.postValue(null)
                                DB.getPlaylistDao().getAll().forEach { playlist ->
                                    playlist.musics.forEach {
                                        if (it.path == music.path) {
                                            playlist.musics.remove(it)
                                            DB.getPlaylistDao().update(playlist)
                                        }
                                    }
                                }
                            } else {
                                Popup(absActivity,
                                    title = getString(R.string.popup_permission_title),
                                    message = getString(R.string.popup_permission_message),
                                    positiveText = getString(R.string.popup_ok),
                                    positiveClick = {
                                        startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
                                    })
                                    .show()
                            }
                        }).show()
                })
                choices.add(Popup.Choice(getString(R.string.popup_playlists)) {
                    showFragment(MusicPlaylistsFragment.getInstance(music))
                })
                Popup(absActivity, choices = choices).showBottom()
            }
        }
    }

    companion object {

        const val ARG_TITLE = "ARG_TITLE"
        const val ARG_MUSICS = "ARG_MUSICS"

        fun getInstance(title: String, musics: List<Music>) = MusicsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putSerializable(ARG_MUSICS, ArrayList(musics))
            }
        }
    }
}