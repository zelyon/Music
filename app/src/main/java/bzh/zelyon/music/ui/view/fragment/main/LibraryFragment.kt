package bzh.zelyon.music.ui.view.fragment.main

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.activityViewModels
import bzh.zelyon.lib.extension.*
import bzh.zelyon.lib.ui.component.CollectionsView
import bzh.zelyon.lib.ui.component.Popup
import bzh.zelyon.lib.ui.view.fragment.AbsToolBarFragment
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Artist
import bzh.zelyon.music.ui.view.fragment.bottom.MusicsFragment
import bzh.zelyon.music.ui.view.fragment.edit.EditArtistFragment
import bzh.zelyon.music.ui.view.viewmodel.LibraryViewModel
import bzh.zelyon.music.util.MusicContent
import bzh.zelyon.music.util.MusicPlayer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.item_artist.view.*

class LibraryFragment: AbsToolBarFragment(), SearchView.OnQueryTextListener {

    private var currentSearch = ""

    private val libraryViewModel: LibraryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (fragment_library_cardview_toolbar.layoutParams as CoordinatorLayout.LayoutParams).topMargin += absActivity.getStatusBarHeight()

        fragment_library_collectionview_artists.headerHeight = absActivity.dpToPx(80) + absActivity.getStatusBarHeight()
        fragment_library_collectionview_artists.thumbMarginTop = absActivity.dpToPx(80) + absActivity.getStatusBarHeight()
        fragment_library_collectionview_artists.helper = ArtistHelper()

        loadMusics()

        libraryViewModel.needReload.observe(absActivity) {
            loadMusics()
        }
    }

    override fun onResume() {
        super.onResume()

        fragment_library_searchview.setOnQueryTextListener(this)
    }

    override fun getIdLayout() = R.layout.fragment_library

    override fun onIdClick(id: Int) {
        when (id) {
            R.id.fragment_library_shuffle -> MusicPlayer.playMusics(MusicContent.getMusics(absActivity).shuffled())
        }
    }

    override fun getIdToolbar() = R.id.fragment_library_toolbar

    override fun getIdMenu() = R.menu.fragment_library

    override fun onQueryTextSubmit(query: String) = true

    override fun onQueryTextChange(query: String): Boolean {
        currentSearch = query
        loadMusics()
        return true
    }

    private fun loadMusics() {
        absActivity.ifPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            if (it) {
                fragment_library_collectionview_artists?.items = MusicContent.getMusicsBySearch(absActivity, currentSearch).toMutableList()
            } else {
                absActivity.showSnackbar(
                    getString(R.string.fragment_library_snackbar_permission_needed),
                    duration = Snackbar.LENGTH_INDEFINITE,
                    actionMessage = getString(R.string.fragment_library_snackbar_permission_grant)) {
                    loadMusics()
                }
            }
        }
    }

    private enum class AnimSate { SHOW, HIDE, FOLLOW_OFFSET }

    inner class ArtistHelper: CollectionsView.Helper() {
        private var animState: AnimSate? = null
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val artist = items[position]
            if (artist is Artist) {
                itemView.item_artist_imageview_artwork.setImage(artist, absActivity.drawableResToDrawable(R.drawable.ic_item_artist))
                itemView.item_artist_imageview_artwork.transitionName = artist.getTransitionName()
                itemView.item_artist_textview_name.text = artist.name
                itemView.item_artist_textview_nbmusic.text = resources.getQuantityString(R.plurals.item_music_nb, artist.musics.size, artist.musics.size)
                itemView.item_artist_button_more.setOnClickListener { onItemLongClick(itemView, items, position) }
            }
        }
        override fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {
            val artist = items[position]
            if (artist is Artist) {
                showFragment(MusicsFragment.getInstance(artist.name, artist.musics))
            }
        }
        override fun onItemLongClick(itemView: View, items: MutableList<*>, position: Int) {
            val artist = items[position]
            if (artist is Artist) {
                val artwork = (itemView.item_artist_imageview_artwork.drawable as? BitmapDrawable)?.bitmap
                val choices = mutableListOf<Popup.Choice>()
                choices.add(Popup.Choice(getString(R.string.popup_play)) {
                    MusicPlayer.playMusics(artist.musics)
                })
                if (MusicPlayer.playingMusic != null) {
                    choices.add(Popup.Choice(getString(R.string.popup_add)) {
                        MusicPlayer.addMusics(artist.musics)
                    })
                }
                choices.add(Popup.Choice(getString(R.string.popup_edit)) {
                    showFragment(EditArtistFragment.getInstance(artist, artwork), transitionView = itemView.item_artist_imageview_artwork)
                })
                Popup(absActivity, choices = choices).showBottom()
            }
        }
        override fun getIndexScroll(items: MutableList<*>, position: Int) = (items[position] as Artist).name.first().toUpperCase().toString()
        override fun onScroll(goUp: Boolean) {
            safeRun {
                val toolbarHeight = fragment_library_cardview_toolbar.height + fragment_library_cardview_toolbar.marginTop + fragment_library_cardview_toolbar.marginBottom
                val collectionViewOffset = fragment_library_collectionview_artists.computeVerticalScrollOffset()
                val neededAnimState = when {
                    goUp -> AnimSate.SHOW
                    collectionViewOffset < toolbarHeight -> AnimSate.FOLLOW_OFFSET
                    else -> AnimSate.HIDE
                }
                if (animState != neededAnimState) {
                    animState = null
                    fragment_library_cardview_toolbar.clearAnimation()
                    fragment_library_cardview_toolbar.animate().apply {
                        translationY(when(neededAnimState) {
                            AnimSate.SHOW -> 0
                            AnimSate.FOLLOW_OFFSET -> -collectionViewOffset
                            AnimSate.HIDE -> -toolbarHeight
                        }.toFloat())
                        withStartAction { animState = neededAnimState }
                        withEndAction {
                            if (animState == AnimSate.FOLLOW_OFFSET) {
                                onScroll(true)
                            }
                            animState = null
                        }
                        duration = if (neededAnimState == AnimSate.FOLLOW_OFFSET) 0 else 400
                    }.start()
                }
            }
        }
    }
}