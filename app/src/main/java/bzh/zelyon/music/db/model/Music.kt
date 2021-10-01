package bzh.zelyon.music.db.model

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import bzh.zelyon.lib.extension.millisecondsToDuration
import bzh.zelyon.music.R
import org.jaudiotagger.audio.mp3.MP3File
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

@Entity(tableName = "music")
data class Music(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artistId")
    val artistId: Int,
    @ColumnInfo(name = "artistName")
    val artistName: String,
    @ColumnInfo(name = "albumId")
    val albumId: Int,
    @ColumnInfo(name = "albumName")
    val albumName: String,
    @ColumnInfo(name = "track")
    val track: Int,
    @ColumnInfo(name = "year")
    val year: Int,
    @ColumnInfo(name = "duration")
    val duration: Int,
    @ColumnInfo(name = "path")
    val path: String): AbsModel() {

    override fun getDeclaration() = title

    override fun getPlaceholderId() = R.drawable.ic_detail_music

    override fun getTransitionName() = id.toString()

    fun getInfos(title: Boolean = true,
                 artist: Boolean = true,
                 album: Boolean = true,
                 duration: Boolean = true,
                 separator: String = " • "): String {
        val infos = mutableListOf<String>()
        if (title) {
            infos.add(this.title)
        }
        if (artist) {
            infos.add(artistName)
        }
        if (album && albumName != File(path).parentFile.name) {
            infos.add(albumName)
        }
        if (duration) {
            infos.add(this.duration.millisecondsToDuration())
        }
        return infos.joinToString(separator)
    }

    fun getArtworkInputStreamFromPath(context: Context): InputStream? {
        var bytes: ByteArray? = null
        var inputStream: InputStream? = null
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(path)
            bytes = mediaMetadataRetriever.embeddedPicture
            mediaMetadataRetriever.release()
            MP3File(path).tag.firstArtwork?.binaryData?.let {
                bytes = it
            }
            val artworkUri = Uri.parse("content://media/external/audio/albumart")
            val artworkUriWithId = ContentUris.withAppendedId(artworkUri, albumId.toLong())
            inputStream = context.contentResolver.openInputStream(artworkUriWithId)
        } catch (ignored: Exception) {
        } finally {
            if (bytes?.isNotEmpty() == true && ByteArrayInputStream(bytes).available() != 0) {
                inputStream = ByteArrayInputStream(bytes)
            }
            return if (inputStream?.available() != 0) inputStream else null
        }
    }
}