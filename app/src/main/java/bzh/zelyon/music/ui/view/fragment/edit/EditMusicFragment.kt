package bzh.zelyon.music.ui.view.fragment.edit

import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import bzh.zelyon.lib.extension.showSnackbar
import bzh.zelyon.lib.ui.component.InputView
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.util.MusicContent
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_edit_music.*
import kotlinx.android.synthetic.main.view_artwork.view.*
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File

class EditMusicFragment private constructor(): AbsEditFragment<Music>() {

    private var audioFile: AudioFile? = null
    private var tag: Tag? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioFile = AudioFileIO.read(File(absModel.path))
        tag = audioFile?.tagOrCreateAndSetDefault

        val artistNames = mutableListOf<String>()
        val albumNames = mutableListOf<String>()
        val genres = mutableListOf<String>()
        MusicContent.getMusics(absActivity).forEach { music ->
            if (!artistNames.contains(music.artistName)) {
                artistNames.add(music.artistName)
            }
            if (!albumNames.contains(music.albumName)) {
                albumNames.add(music.albumName)
            }
        }
        absActivity.contentResolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null, null, null, null)?.use {
            while (it.moveToNext()) {
                genres.add(it.getString(it.getColumnIndex(MediaStore.Audio.Genres.NAME)))
            }
            it.close()
        }

        fragment_edit_music_inputview_title.text = absModel.title
        fragment_edit_music_inputview_artist.text = absModel.artistName
        fragment_edit_music_inputview_artist.choices = artistNames.map { InputView.Choice(it, it, absModel.artistName == it) }.toMutableList()
        fragment_edit_music_inputview_album.text = absModel.albumName
        fragment_edit_music_inputview_album.choices = albumNames.map { InputView.Choice(it, it, absModel.albumName == it) }.toMutableList()
        fragment_edit_music_inputview_track.text = absModel.track.toString()
        fragment_edit_music_inputview_year.text = absModel.year.toString()
        fragment_edit_music_inputview_genre.text = tag?.getFirst(FieldKey.GENRE) ?: ""
        fragment_edit_music_inputview_genre.choices = genres.map { InputView.Choice(it, it, tag?.getFirst(FieldKey.GENRE) ?: "" == it) }.toMutableList()

        editViewModel.getMusic(absModel.artistName, absModel.title).observe(viewLifecycleOwner, { musicResponseFr  ->
            infosFromLastFM = musicResponseFr?.track?.wiki?.content.orEmpty()
            imageUrlFromLastFM = musicResponseFr?.track?.album?.image?.get(3)?.text
            if (infosFromLastFM.isNullOrBlank()) {
                editViewModel.getMusic(absModel.artistName, absModel.title, false).observe(viewLifecycleOwner, { musicResponseEn ->
                    infosFromLastFM = musicResponseEn?.track?.wiki?.content.orEmpty()
                })
            }
        })
    }

    override fun onClickArtwork() {
        BottomSheetDialog(absActivity).apply {
            setContentView(LayoutInflater.from(absActivity).inflate(R.layout.view_artwork, null, false).apply {
                view_artwork_device.setOnClickListener {
                    getImageOnDevice()
                    dismiss()
                }
                view_artwork_download.isVisible = !imageUrlFromLastFM.isNullOrBlank()
                view_artwork_download.setOnClickListener {
                    getImageFromLastFM()
                    dismiss()
                }
                view_artwork_delete.setOnClickListener {
                    deleteArtwork()
                    dismiss()
                }
            })
        }.show()
    }

    override fun onSave() {
        try {
            tag?.setField(FieldKey.TITLE, fragment_edit_music_inputview_title.text)
            tag?.setField(FieldKey.ARTIST, fragment_edit_music_inputview_artist.selectedChoice?.value.toString())
            tag?.setField(FieldKey.ALBUM, fragment_edit_music_inputview_album.selectedChoice?.value.toString())
            tag?.setField(FieldKey.TRACK, fragment_edit_music_inputview_track.number.toString())
            tag?.setField(FieldKey.YEAR, fragment_edit_music_inputview_year.number.toString())
            tag?.setField(FieldKey.GENRE, fragment_edit_music_inputview_genre.selectedChoice?.value.toString())
            if (deleteCurrentArtwork) {
                tag?.deleteArtworkField()
            }
            newArtwork?.let { artwork ->
                tag?.setField(artwork)
            }
        } catch (e: Exception) {
            absActivity.showSnackbar(getString(R.string.fragment_edit_snackbar_failed))
        } finally {
            audioFile?.commit()
            MediaScannerConnection.scanFile(absActivity, arrayOf(absModel.path), null) { _, _ -> back() }
        }
    }

    override fun getFormLayoutId() = R.layout.fragment_edit_music

    companion object {

        fun getInstance(music: Music, artwork: Bitmap?) = EditMusicFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_ABS_MODEL, music)
                putParcelable(ARG_ARTORK, artwork)
            }
        }
    }
}