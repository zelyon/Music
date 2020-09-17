package bzh.zelyon.music.ui.view.fragment.main

import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import bzh.zelyon.libcommon.extension.millisecondsToDuration
import bzh.zelyon.libcommon.extension.setImage
import bzh.zelyon.libcommon.ui.component.CollectionsView
import bzh.zelyon.libcommon.ui.view.fragment.AbsToolBarFragment
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.ui.view.fragment.bottom.MusicPlaylistsFragment
import bzh.zelyon.music.ui.view.fragment.edit.EditMusicFragment
import bzh.zelyon.music.util.MusicPlayer
import kotlinx.android.synthetic.main.fragment_playing.*
import kotlinx.android.synthetic.main.item_music.view.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.max
import kotlin.math.min

class PlayingFragment: AbsToolBarFragment() {

    private var playingMusic: Music? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_playing_seekbar_current.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) = MusicPlayer.goTo(seekBar.progress)
        })
        fragment_playing_itemsview_musics.helper = MusicHelper()
        fragment_playing_itemsview_musics.items = MusicPlayer.musics

        fixedRateTimer(period = 400) {
            safeRun {
                MusicPlayer.playingMusic?.let {
                    fragment_playing_textview_duration.text = MusicPlayer.duration.millisecondsToDuration()
                    fragment_playing_textview_current.text = MusicPlayer.currentPosition.millisecondsToDuration()
                    fragment_playing_seekbar_current.max = MusicPlayer.duration
                    fragment_playing_seekbar_current.progress = MusicPlayer.currentPosition
                    fragment_playing_imagebutton_previous.isVisible = MusicPlayer.playingPositions.size > 1
                    fragment_playing_imagebutton_next.isVisible = MusicPlayer.isShuffle || MusicPlayer.playingPosition < MusicPlayer.musics.size - 1
                    fragment_playing_imagebutton_repeat.alpha = if (MusicPlayer.isRepeat) 1F else 0.5F
                    fragment_playing_imagebutton_shuffle.alpha = if (MusicPlayer.isShuffle) 1F else 0.5F
                    if (playingMusic != MusicPlayer.playingMusic) {
                        playingMusic = MusicPlayer.playingMusic
                        fragment_playing_itemsview_musics.notifyDataSetChanged()
                        updateToolBar()
                        val itemsManager = fragment_playing_itemsview_musics.layoutManager as LinearLayoutManager
                        val firstPositionVisible = itemsManager.findFirstVisibleItemPosition()
                        val lastPositionVisible = itemsManager.findLastVisibleItemPosition()
                        when {
                            MusicPlayer.playingPosition < firstPositionVisible -> MusicPlayer.playingPosition + 1
                            MusicPlayer.playingPosition > lastPositionVisible -> MusicPlayer.playingPosition + (lastPositionVisible - firstPositionVisible)/2
                            else -> null
                        }?.let {
                            fragment_playing_itemsview_musics.smoothScrollToPosition(it)
                        }
                    }
                } ?: back()
            }
        }
    }

    override fun getIdLayout() = R.layout.fragment_playing

    override fun onIdClick(id: Int) {
        super.onIdClick(id)
        when (id) {
            R.id.fragment_playing_imagebutton_previous -> MusicPlayer.previous()
            R.id.fragment_playing_imagebutton_next -> MusicPlayer.next()
            R.id.fragment_playing_imagebutton_repeat -> MusicPlayer.isRepeat = !MusicPlayer.isRepeat
            R.id.fragment_playing_imagebutton_shuffle -> MusicPlayer.isShuffle = !MusicPlayer.isShuffle
        }
    }

    override fun getIdToolbar() = R.id.fragment_playing_toolbar

    override fun getTitleToolBar() = MusicPlayer.playingMusic?.getInfos(
        title = true,
        artist = false,
        album = false,
        duration = false
    ).orEmpty()

    override fun getSubTitleToolBar() = MusicPlayer.playingMusic?.getInfos(
        title = false,
        artist = true,
        album = true,
        duration = false
    ).orEmpty()

    inner class MusicHelper: CollectionsView.Helper() {
        override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
            val music = items[position]
            if (music is Music) {
                val isPlaying = MusicPlayer.playingPosition == position
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
                fragment_playing_itemsview_musics.notifyDataSetChanged()
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
                    menu.findItem(R.id.item_delete).isVisible = false
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.item_edit -> showFragment(EditMusicFragment.getInstance(music, artwork), transitionView = itemView.item_music_imageview_artwork)
                            R.id.item_playlists -> showFragment(MusicPlaylistsFragment.getInstance(music, null))
                        }
                        true
                    }
                }.show()
            }
        }
        override fun getDragView(itemView: View, items: MutableList<*>, position: Int): View? = itemView.item_music_imagebutton
        override fun onItemsMove(itemView: View, items: MutableList<*>, fromPosition: Int, toPosition: Int) {
            MusicPlayer.musics = items.map { it as Music }.toMutableList()
            if (MusicPlayer.playingPosition == fromPosition) {
                MusicPlayer.playingPosition = toPosition
            } else if (MusicPlayer.playingPosition in min(fromPosition, toPosition)..max(fromPosition, toPosition)) {
                if (fromPosition > toPosition) {
                    MusicPlayer.playingPosition++
                } else {
                    MusicPlayer.playingPosition--
                }
            }
        }
        override fun onItemSwipe(itemView: View, items: MutableList<*>, position: Int) {
            MusicPlayer.musics = items.map { it as Music }.toMutableList()
            if (position < MusicPlayer.playingPosition) {
                MusicPlayer.playingPosition--
            } else if (position == MusicPlayer.playingPosition) {
                MusicPlayer.run()
                updateToolBar()
            }
        }
    }
}