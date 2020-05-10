package bzh.zelyon.music.ui.view.fragment.main

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Artist
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.extension.closeKeyboard
import bzh.zelyon.music.extension.dpToPx
import bzh.zelyon.music.extension.setImage
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsToolBarFragment
import bzh.zelyon.music.ui.view.fragment.bottom.MusicsFragment
import bzh.zelyon.music.ui.view.fragment.edit.EditArtistFragment
import bzh.zelyon.music.util.MusicContent
import bzh.zelyon.music.util.MusicPlayer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.item_artist.view.*

class LibraryFragment: AbsToolBarFragment(), MusicPlayer.Listener, SearchView.OnQueryTextListener {

    private var currentSearch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicPlayer.listeners.add(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_library_searchview.setOnQueryTextListener(this)

        fragment_library_itemsview_artists.apply {
            nbColumns = 2
            spaceDivider = absActivity.dpToPx(8).toInt()
            idLayoutItem = R.layout.item_artist
            idLayoutHeader = R.layout.item_artist_header_footer
            idLayoutFooter = R.layout.item_artist_header_footer
            idLayoutEmpty = R.layout.item_artist_empty
            isFastScroll = true
            thumbMarginTop = absActivity.dpToPx(80)
            thumbMarginBottom = absActivity.dpToPx(80)
            helper = ArtistHelper()
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (scrollState == 0) {
                        closeKeyboard()
                    }
                }
            })
        }

        loadMusics()
    }

    override fun getLayoutId() = R.layout.fragment_library

    override fun onIdClick(id: Int) {
        when (id) {
            R.id.fragment_library_shuffle -> {
                MusicPlayer.playMusics(MusicContent.getMusics(absActivity).shuffled())
            }
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

    override fun onMusicFileDeleted(music: Music) {
        safeRun {
            loadMusics()
        }
    }

    private fun loadMusics() {
        absActivity.ifPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            if (it) {
                fragment_library_itemsview_artists.items = MusicContent.getMusicsBySearch(absActivity, currentSearch).toMutableList()
            } else {
                absActivity.showSnackbar(
                    getString(R.string.fragment_library_snackbar_permission_needed),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.fragment_library_snackbar_permission_grant)) {
                    loadMusics()
                }
            }
        }
    }

    private enum class AnimSate { SHOW, HIDE, FOLLOW_OFFSET }

    inner class ArtistHelper: ItemsView.Helper() {
        private var animState: AnimSate? = null
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val artist = items[position]
            if (artist is Artist) {
                itemView.item_artist_imageview_artwork.setImage(artist, absActivity.getDrawable(R.drawable.ic_artist))
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
                PopupMenu(absActivity, itemView.item_artist_button_more).apply {
                    menuInflater.inflate(R.menu.item, menu)
                    menu.findItem(R.id.item_add).isVisible = MusicPlayer.playingMusic != null
                    menu.findItem(R.id.item_delete).isVisible = false
                    menu.findItem(R.id.item_playlists).isVisible = false
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_play -> MusicPlayer.playMusics(artist.musics)
                            R.id.item_add -> MusicPlayer.addMusics(artist.musics)
                            R.id.item_edit_infos -> showFragment(EditArtistFragment.getInstance(artist, artwork), transitionView = itemView.item_artist_imageview_artwork)
                        }
                        return@setOnMenuItemClickListener true
                    }
                }.show()
            }
        }
        override fun getIndexScroll(items: MutableList<*>, position: Int) = (items[position] as Artist).name.first().toUpperCase().toString()
        override fun onScroll(goUp: Boolean) {
            safeRun {
                val toolbarHeight = fragment_library_cardview_toolbar.height + fragment_library_cardview_toolbar.marginTop + fragment_library_cardview_toolbar.marginBottom
                val itemsViewOffset = fragment_library_itemsview_artists.computeVerticalScrollOffset()
                val neededAnimState = when {
                    goUp -> AnimSate.SHOW
                    itemsViewOffset < toolbarHeight -> AnimSate.FOLLOW_OFFSET
                    else -> AnimSate.HIDE
                }
                if (animState != neededAnimState) {
                    animState = null
                    fragment_library_cardview_toolbar.clearAnimation()
                    fragment_library_cardview_toolbar.animate().apply {
                        translationY(when(neededAnimState) {
                            AnimSate.SHOW -> 0
                            AnimSate.FOLLOW_OFFSET -> -itemsViewOffset
                            else -> -toolbarHeight
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