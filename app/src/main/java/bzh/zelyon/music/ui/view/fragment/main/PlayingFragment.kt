package bzh.zelyon.music.ui.view.fragment.main

import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import bzh.zelyon.lib.extension.*
import bzh.zelyon.lib.ui.component.CollectionsView
import bzh.zelyon.lib.ui.component.Popup
import bzh.zelyon.lib.ui.view.fragment.AbsFragment
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

class PlayingFragment: AbsFragment() {

    private var playingMusic: Music? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_playing_seekbar_current.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) = MusicPlayer.goTo(seekBar.progress)
        })
        fragment_playing_collectionview_musics.headerHeight = absActivity.getStatusBarHeight().toFloat()
        fragment_playing_collectionview_musics.thumbMarginTop = absActivity.getStatusBarHeight().toFloat()
        fragment_playing_collectionview_musics.helper = MusicHelper()
        fragment_playing_collectionview_musics.items = MusicPlayer.musics

        fixedRateTimer(period = 400) {
            safeRun {
                MusicPlayer.playingMusic?.let {
                    fragment_playing_textview_duration.text = MusicPlayer.duration.millisecondsToDuration()
                    fragment_playing_textview_current.text = MusicPlayer.currentPosition.millisecondsToDuration()
                    fragment_playing_seekbar_current.max = MusicPlayer.duration
                    fragment_playing_seekbar_current.progress = MusicPlayer.currentPosition
                    fragment_playing_imagebutton_previous.isVisible = MusicPlayer.hasPrevious
                    fragment_playing_imagebutton_next.isVisible = MusicPlayer.hasNext
                    fragment_playing_imagebutton_repeat.alpha = if (MusicPlayer.isRepeat) 1F else 0.5F
                    fragment_playing_imagebutton_shuffle.alpha = if (MusicPlayer.isShuffle) 1F else 0.5F
                    if (playingMusic != MusicPlayer.playingMusic) {
                        playingMusic?.let { music ->
                            fragment_playing_collectionview_musics.refreshItem(music)
                        }
                        MusicPlayer.playingMusic?.let { music ->
                            fragment_playing_collectionview_musics.refreshItem(music)
                        }
                        playingMusic = MusicPlayer.playingMusic
                        val itemsManager = fragment_playing_collectionview_musics.layoutManager as LinearLayoutManager
                        val firstPositionVisible = itemsManager.findFirstVisibleItemPosition()
                        val lastPositionVisible = itemsManager.findLastVisibleItemPosition()
                        when {
                            MusicPlayer.playingPosition < firstPositionVisible -> MusicPlayer.playingPosition + 1
                            MusicPlayer.playingPosition > lastPositionVisible -> MusicPlayer.playingPosition + (lastPositionVisible - firstPositionVisible)/2
                            else -> null
                        }?.let {
                            fragment_playing_collectionview_musics.smoothScrollToPosition(it)
                        }
                    }
                } ?: absActivity.back()
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
                itemView.item_music_imageview_artwork.setImage(music, absActivity.drawableResToDrawable(R.drawable.ic_item_music))
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
                itemView.item_music_imagebutton.setImageResource(R.drawable.ic_item_drag)
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
                val choices = mutableListOf<Popup.Choice>()
                choices.add(Popup.Choice(getString(R.string.popup_edit)) {
                    absActivity.actionFragment(EditMusicFragment.getInstance(music, artwork), transitionView = itemView.item_music_imageview_artwork)
                })
                choices.add(Popup.Choice(getString(R.string.popup_playlists)) {
                    absActivity.showFragment(MusicPlaylistsFragment.getInstance(music))
                })
                Popup(absActivity, choices = choices).showBottom()
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
            }
        }
    }
}