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
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsToolBarFragment
import bzh.zelyon.music.ui.view.fragment.bottom.MusicPlaylistsFragment
import bzh.zelyon.music.ui.view.fragment.edit.EditMusicFragment
import bzh.zelyon.music.utils.MusicManager
import bzh.zelyon.music.utils.dpToPx
import bzh.zelyon.music.utils.millisecondstoDuration
import bzh.zelyon.music.utils.setImage
import kotlinx.android.synthetic.main.fragment_playing.*
import kotlinx.android.synthetic.main.item_music.view.*
import kotlin.concurrent.fixedRateTimer

class PlayingFragment: AbsToolBarFragment(), SeekBar.OnSeekBarChangeListener, MusicManager.Listener {

    private var currentMusicId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicManager.listeners.add(this)
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
            items = MusicManager.musics
        }

        fixedRateTimer(period = 400) {
            safeRun {
                MusicManager.currentMusic?.let { currentMusic ->
                    if(currentMusicId != currentMusic.id) {
                        currentMusicId = currentMusic.id
                        fragment_playing_itemsview_musics.notifyDataSetChanged()
                        updateToolBar()
                        val musicPosition = MusicManager.musics.indexOf(currentMusic) + 1
                        val itemsManager = fragment_playing_itemsview_musics.layoutManager as LinearLayoutManager
                        val firstPositionVisible = itemsManager.findFirstVisibleItemPosition()
                        val lastPositionVisible = itemsManager.findLastVisibleItemPosition()
                        when {
                            musicPosition < firstPositionVisible -> musicPosition
                            musicPosition > lastPositionVisible -> musicPosition + (lastPositionVisible - firstPositionVisible)/2
                            else -> null
                        }?.let {
                            fragment_playing_itemsview_musics.smoothScrollToPosition(it)
                        }
                    }
                    fragment_playing_textview_duration.text = MusicManager.duration.millisecondstoDuration()
                    fragment_playing_textview_current.text = MusicManager.currentPosition.millisecondstoDuration()
                    fragment_playing_seekbar_current.max = MusicManager.duration
                    fragment_playing_seekbar_current.progress = MusicManager.currentPosition
                    fragment_playing_imagebutton_previous.isVisible = MusicManager.previousExist
                    fragment_playing_imagebutton_next.isVisible = MusicManager.nextExist
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
            R.id.fragment_playing_imagebutton_previous -> MusicManager.previous()
            R.id.fragment_playing_imagebutton_next -> MusicManager.next()
            R.id.fragment_playing_imagebutton_shuffle -> {
                MusicManager.shuffle()
                fragment_playing_itemsview_musics.notifyDataSetChanged()
            }
        }
    }

    override fun getIdToolbar() = R.id.fragment_playing_toolbar

    override fun getToolBarTitle() = MusicManager.currentMusic?.getInfos(
        title = true,
        artist = false,
        album = false,
        duration = false
    ).orEmpty()

    override fun getToolBarSubTitle() = MusicManager.currentMusic?.getInfos(
        title = false,
        artist = true,
        album = true,
        duration = false
    ).orEmpty()

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) = MusicManager.goTo(seekBar.progress)

    override fun onMusicFileDeleted(music: Music) {
        safeRun {
            MusicManager.musics.remove(music)
            if (MusicManager.currentMusic?.id == music.id) {
                MusicManager.previous()
            } else {
                MusicManager.updateMusicIndex()
            }
            fragment_playing_itemsview_musics.items = MusicManager.musics
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
                MusicManager.jumpTo(music)
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
                            R.id.item_delete -> MusicManager.deleteMusicFile(absActivity, music)
                            R.id.item_playlists -> showFragment(MusicPlaylistsFragment.getInstance(music))
                        }
                        true
                    }
                }.show()
            }
        }
        override fun getDragView(itemView: View, items: MutableList<*>, position: Int): View? = itemView.item_music_imagebutton
        override fun onItemsMove(items: MutableList<*>) {
            MusicManager.musics = items.map { it as Music }.toMutableList()
            MusicManager.updateMusicIndex()
        }
        override fun onItemSwipe(itemView: View, items: MutableList<*>, position: Int) {
            MusicManager.musics = items.map { it as Music }.toMutableList()
            if (MusicManager.musicPosition == position) {
                MusicManager.previous()
            } else {
                MusicManager.updateMusicIndex()
            }
        }
    }
}