
package bzh.zelyon.music.ui.view.fragment.bottom

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import bzh.zelyon.lib.extension.*
import bzh.zelyon.lib.ui.component.CollectionsView
import bzh.zelyon.lib.ui.component.Popup
import bzh.zelyon.lib.ui.view.fragment.AbsToolBarBottomSheetFragment
import bzh.zelyon.music.BuildConfig
import bzh.zelyon.music.R
import bzh.zelyon.music.db.DB
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.ui.view.activity.MainActivity
import bzh.zelyon.music.ui.view.fragment.edit.EditMusicFragment
import bzh.zelyon.music.ui.view.viewmodel.LibraryViewModel
import bzh.zelyon.music.util.MusicPlayer
import kotlinx.android.synthetic.main.fragment_musics.*
import kotlinx.android.synthetic.main.item_music.view.*
import java.io.File

class MusicsFragment private constructor(): AbsToolBarBottomSheetFragment() {

    lateinit var musics: List<Music>

    private val libraryViewModel: LibraryViewModel by activityViewModels()

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

    override fun getIdLayout() = R.layout.fragment_musics

    override fun getTitleToolBar() = arguments?.getString(ARG_TITLE).orEmpty()

    inner class MusicHelper: CollectionsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                itemView.item_music_imageview_artwork.setImage(
                    music,
                    absActivity.drawableResToDrawable(R.drawable.ic_item_music),
                    resizeInPx = Pair(absActivity.dpToPx(48).toInt(), absActivity.dpToPx(48).toInt()))
                itemView.item_music_imageview_artwork.transitionName = music.getTransitionName()
                itemView.item_music_textview_title.text = music.title
                itemView.item_music_textview_infos.text = music.getInfos(
                    title = false,
                    artist = false,
                    album = true,
                    duration = true
                )
                itemView.item_music_imagebutton.setImageResource(R.drawable.ic_item_more)
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
                    absActivity.actionFragment(EditMusicFragment.getInstance(music, artwork), transitionView = itemView.item_music_imageview_artwork)
                    back()
                })
                choices.add(Popup.Choice(getString(R.string.popup_delete)) {
                    Popup(absActivity,
                        title = getString(R.string.popup_delete),
                        message = getString(R.string.popup_delete_message, music.title),
                        positiveText = getString(R.string.popup_yes),
                        positiveClick = {
                            deleteMusic(music)
                        }).show()
                })
                choices.add(Popup.Choice(getString(R.string.popup_playlists)) {
                    absActivity.showFragment(MusicPlaylistsFragment.getInstance(music))
                })
                Popup(absActivity, choices = choices).showBottom()
            }
        }

        private fun deleteMusic(music: Music) {
            absActivity.launchPermissionFiles(BuildConfig.APPLICATION_ID) {
                if (it) {
                    File(music.path).delete()
                    back()
                    libraryViewModel.needReload.postValue(null)
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
                            deleteMusic(music)
                        })
                        .show()
                }
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