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

    private var playingMusic: Music? = null
    private var playingPosition = -1

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
        fragment_playing_imagebutton_repeat.alpha = if (MusicPlayer.isRepeat) 1F else 0.5F
        fragment_playing_imagebutton_shuffle.alpha = if (MusicPlayer.isShuffle) 1F else 0.5F

        fixedRateTimer(period = 400) {
            safeRun {
                MusicPlayer.playingMusic?.let {
                    if (playingMusic != MusicPlayer.playingMusic || playingPosition != MusicPlayer.playingPosition) {
                        playingPosition = MusicPlayer.playingPosition
                        playingMusic = MusicPlayer.playingMusic
                        fragment_playing_itemsview_musics.notifyDataSetChanged()
                        updateToolBar()
                        val itemsManager = fragment_playing_itemsview_musics.layoutManager as LinearLayoutManager
                        val firstPositionVisible = itemsManager.findFirstVisibleItemPosition()
                        val lastPositionVisible = itemsManager.findLastVisibleItemPosition()
                        when {
                            playingPosition < firstPositionVisible -> playingPosition + 1
                            playingPosition > lastPositionVisible -> playingPosition + (lastPositionVisible - firstPositionVisible)/2
                            else -> null
                        }?.let {
                            fragment_playing_itemsview_musics.smoothScrollToPosition(it)
                        }
                    }
                    fragment_playing_textview_duration.text = MusicPlayer.duration.millisecondstoDuration()
                    fragment_playing_textview_current.text = MusicPlayer.currentPosition.millisecondstoDuration()
                    fragment_playing_seekbar_current.max = MusicPlayer.duration
                    fragment_playing_seekbar_current.progress = MusicPlayer.currentPosition
                    fragment_playing_imagebutton_previous.isVisible = playingPosition > 0
                    fragment_playing_imagebutton_next.isVisible = playingPosition < MusicPlayer.musics.size - 1
                } ?: back()
            }
        }
    }

    override fun getLayoutId() = R.layout.fragment_playing

    override fun onIdClick(id: Int) {
        super.onIdClick(id)
        when (id) {
            R.id.fragment_playing_imagebutton_previous -> MusicPlayer.previous()
            R.id.fragment_playing_imagebutton_next -> MusicPlayer.next()
            R.id.fragment_playing_imagebutton_repeat -> {
                MusicPlayer.isRepeat = !MusicPlayer.isRepeat
                fragment_playing_imagebutton_repeat.alpha = if (MusicPlayer.isRepeat) 1F else 0.5F
            }
            R.id.fragment_playing_imagebutton_shuffle -> {
                MusicPlayer.isShuffle = !MusicPlayer.isShuffle
                fragment_playing_imagebutton_shuffle.alpha = if (MusicPlayer.isShuffle) 1F else 0.5F
            }
        }
    }

    override fun getIdToolbar() = R.id.fragment_playing_toolbar

    override fun getToolBarTitle() = MusicPlayer.playingMusic?.getInfos(
        title = true,
        artist = false,
        album = false,
        duration = false
    ).orEmpty()

    override fun getToolBarSubTitle() = MusicPlayer.playingMusic?.getInfos(
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
            val position = MusicPlayer.musics.indexOf(music)
            if (position < playingPosition || position == playingPosition && position > 0) {
                playingPosition--
                MusicPlayer.playingPosition--
            }
            if (position == playingPosition) {
                MusicPlayer.run()
            }
            MusicPlayer.musics.remove(music)
            fragment_playing_itemsview_musics.items = MusicPlayer.musics
        }
    }

    inner class MusicHelper: ItemsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                val isPlaying = playingPosition == position
                val alpha = if (isPlaying) 1f else .5f
                val typeface = if (isPlaying) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                itemView.item_music_imageview_artwork.alpha = alpha
                itemView.item_music_textview_title.alpha = alpha
                itemView.item_music_textview_infos.alpha = alpha
                itemView.item_music_imageview_artwork.setImage(music, absActivity.getDrawable(R.drawable.ic_music))
                itemView.item_music_imageview_artwork.transitionName = music.id.toString()
                itemView.item_music_textview_title.text = music.title
                itemView.item_music_textview_title.typeface = typeface
                itemView.item_music_textview_infos.text = music.getInfos(
                    title = false,
                    artist = true,
                    album = true,
                    duration = true
                )
                itemView.item_music_textview_infos.typeface = typeface
                itemView.item_music_imagebutton.setImageResource(R.drawable.ic_drag)
            }
        }
        override fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                MusicPlayer.jumpTo(position)
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
        override fun onItemsMove(itemView: View, items: MutableList<*>, fromPosition: Int, toPosition: Int) {
            if (playingPosition == fromPosition) {
                playingPosition = toPosition
                MusicPlayer.playingPosition = playingPosition
                playingMusic = MusicPlayer.playingMusic
            } else if (playingPosition in fromPosition..toPosition) {
                playingPosition--
                MusicPlayer.playingPosition = playingPosition
                playingMusic = MusicPlayer.playingMusic
            }
            MusicPlayer.musics = items.map { it as Music }.toMutableList()
        }
        override fun onItemSwipe(itemView: View, items: MutableList<*>, position: Int) {
            if (position < playingPosition || position == playingPosition && position > 0) {
                playingPosition--
                MusicPlayer.playingPosition = playingPosition
                playingMusic = MusicPlayer.playingMusic
            }
            if (position == playingPosition) {
                MusicPlayer.run()
            }
            updateToolBar()
            MusicPlayer.musics = items.map { it as Music }.toMutableList()
        }
    }
}