package bzh.zelyon.music.ui.view.fragment.main

import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.extension.dpToPx
import bzh.zelyon.music.extension.millisecondstoDuration
import bzh.zelyon.music.extension.setImage
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsToolBarFragment
import bzh.zelyon.music.ui.view.fragment.bottom.MusicPlaylistsFragment
import bzh.zelyon.music.ui.view.fragment.edit.EditMusicFragment
import bzh.zelyon.music.util.MusicPlayer
import kotlinx.android.synthetic.main.fragment_playing.*
import kotlinx.android.synthetic.main.item_music.view.*
import kotlin.concurrent.fixedRateTimer

class PlayingFragment: AbsToolBarFragment(), SeekBar.OnSeekBarChangeListener, MusicPlayer.Listener {

    private var currentMusicId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicPlayer.listeners.add(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_playing_seekbar_current.setOnSeekBarChangeListener(this)
        fragment_playing_itemsview_musics.apply {
            idLayoutItem = R.layout.item_music
            idLayoutFooter = R.layout.item_music_footer
            isFastScroll = true
            dragNDropEnable = true
            swipeEnable = true
            thumbMarginBottom = absActivity.dpToPx(140)
            helper = MusicHelper()
            items = MusicPlayer.musics
        }

        fixedRateTimer(period = 400) {
            safeRun {
                MusicPlayer.currentMusic?.let { currentMusic ->
                    val musicPosition = MusicPlayer.musics.indexOf(currentMusic)
                    if(currentMusicId != currentMusic.id) {
                        currentMusicId = currentMusic.id
                        fragment_playing_itemsview_musics.notifyDataSetChanged()
                        updateToolBar()
                        val itemsManager = fragment_playing_itemsview_musics.layoutManager as LinearLayoutManager
                        val firstPositionVisible = itemsManager.findFirstVisibleItemPosition()
                        val lastPositionVisible = itemsManager.findLastVisibleItemPosition()
                        when {
                            musicPosition < firstPositionVisible -> musicPosition + 1
                            musicPosition > lastPositionVisible -> musicPosition + (lastPositionVisible - firstPositionVisible)/2
                            else -> null
                        }?.let {
                            fragment_playing_itemsview_musics.smoothScrollToPosition(it)
                        }
                    }
                    fragment_playing_textview_duration.text = MusicPlayer.duration.millisecondstoDuration()
                    fragment_playing_textview_current.text = MusicPlayer.currentPosition.millisecondstoDuration()
                    fragment_playing_seekbar_current.max = MusicPlayer.duration
                    fragment_playing_seekbar_current.progress = MusicPlayer.currentPosition
                    fragment_playing_imagebutton_previous.isVisible = musicPosition > 0
                    fragment_playing_imagebutton_next.isVisible = musicPosition < MusicPlayer.musics.size - 1
                } ?: run {
                    back()
                }
            }
        }
    }

    override fun getLayoutId() = R.layout.fragment_playing

    override fun onIdClick(id: Int) {
        super.onIdClick(id)
        when (id) {
            R.id.fragment_playing_imagebutton_previous -> MusicPlayer.previous()
            R.id.fragment_playing_imagebutton_next -> MusicPlayer.next()
            R.id.fragment_playing_imagebutton_shuffle -> {
                MusicPlayer.shuffle()
                fragment_playing_itemsview_musics.notifyDataSetChanged()
            }
        }
    }

    override fun getIdToolbar() = R.id.fragment_playing_toolbar

    override fun getToolBarTitle() = MusicPlayer.currentMusic?.getInfos(
        title = true,
        artist = false,
        album = false,
        duration = false
    ).orEmpty()

    override fun getToolBarSubTitle() = MusicPlayer.currentMusic?.getInfos(
        title = false,
        artist = true,
        album = true,
        duration = false
    ).orEmpty()

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) = MusicPlayer.goTo(seekBar.progress)

    override fun onMusicFileDeleted(music: Music) {
        safeRun {
            MusicPlayer.musics.remove(music)
            if (MusicPlayer.currentMusic?.id == music.id) {
                MusicPlayer.previous()
            }
            fragment_playing_itemsview_musics.items = MusicPlayer.musics
        }
    }

    inner class MusicHelper: ItemsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                val alpha = if (currentMusicId == music.id) 1f else .5f
                itemView.item_music_imageview_artwork.alpha = alpha
                itemView.item_music_textview_title.alpha = alpha
                itemView.item_music_textview_infos.alpha = alpha
                itemView.item_music_imageview_artwork.setImage(music, absActivity.getDrawable(R.drawable.ic_music))
                itemView.item_music_imageview_artwork.transitionName = music.id.toString()
                itemView.item_music_textview_title.text = music.title
                itemView.item_music_textview_title.typeface = if (currentMusicId == music.id) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                itemView.item_music_textview_infos.text = music.getInfos(
                    title = false,
                    artist = true,
                    album = true,
                    duration = true
                )
                itemView.item_music_textview_infos.typeface = if (currentMusicId == music.id) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                itemView.item_music_imagebutton.setImageResource(R.drawable.ic_drag)
            }
        }
        override fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                MusicPlayer.jumpTo(music)
            }
        }
        override fun onItemLongClick(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                val artwork = (itemView.item_music_imageview_artwork.drawable as? BitmapDrawable)?.bitmap
                PopupMenu(absActivity, itemView).apply {
                    menuInflater.inflate(R.menu.item, menu)
                    menu.findItem(R.id.item_play).isVisible = false
                    menu.findItem(R.id.item_add).isVisible = false
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_edit_infos -> showFragment(EditMusicFragment.getInstance(music, artwork), transitionView = itemView.item_music_imageview_artwork)
                            R.id.item_delete -> MusicPlayer.deleteMusicFile(absActivity, music)
                            R.id.item_playlists -> showFragment(MusicPlaylistsFragment.getInstance(music))
                        }
                        true
                    }
                }.show()
            }
        }
        override fun getDragView(itemView: View, items: MutableList<*>, position: Int): View? = itemView.item_music_imagebutton
        override fun onItemsMove(items: MutableList<*>) {
            MusicPlayer.musics = items.map { it as Music }.toMutableList()
        }
        override fun onItemSwipe(itemView: View, items: MutableList<*>, position: Int) {
            MusicPlayer.musics = items.map { it as Music }.toMutableList()
            if (MusicPlayer.musics.indexOf(MusicPlayer.currentMusic) == position) {
                MusicPlayer.previous()
            }
        }
    }
}