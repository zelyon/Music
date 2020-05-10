package bzh.zelyon.music.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import bzh.zelyon.music.db.model.Artist
import bzh.zelyon.music.db.model.Music
import java.io.File

object MusicContent {

    fun getMusics(context: Context, query: String = "1 = 1", vararg args: String): List<Music> {
        val musics = mutableListOf<Music>()
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.DATA
        )
        val selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " = 1 AND ($query)"
        val order = MediaStore.Audio.AudioColumns.ARTIST + ", " +
                MediaStore.Audio.AudioColumns.YEAR + "," +
                MediaStore.Audio.AudioColumns.ALBUM + ", " +
                MediaStore.Audio.AudioColumns.TRACK  + ", " +
                MediaStore.Audio.AudioColumns.TITLE +
                " COLLATE NOCASE ASC"
        context.contentResolver?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, args, order)?.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex(MediaStore.Audio.Media._ID))
                val title = it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artistId = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                val artistName = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val albumId = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val albumName = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val track = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.TRACK))
                val year = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.YEAR))
                val duration = it.getInt(it.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val path = it.getString(it.getColumnIndex(MediaStore.Audio.Media.DATA))
                if (File(path).exists()) {
                    musics.add(Music(id, title, artistId, artistName, albumId, albumName, track, year, duration, path))
                }
            }
            it.close()
        }
        return musics
    }

    fun getMusicsBySearch(context: Context, search: String): List<Artist> {
        val artists = mutableMapOf<String, Artist>()
        val query = MediaStore.Audio.AudioColumns.TITLE + " LIKE ? OR " +
                MediaStore.Audio.AudioColumns.ARTIST + " LIKE ? OR " +
                MediaStore.Audio.AudioColumns.ALBUM + " LIKE ? OR " +
                MediaStore.Audio.AudioColumns.YEAR + " LIKE ? "
        getMusics(context, query, "%$search%", "%$search%", "%$search%", "%$search%").forEach { music ->
            if (artists.containsKey(music.artistName)) {
                artists.getValue(music.artistName).musics.add(music)
            } else {
                artists[music.artistName] = Artist(music.artistId, music.artistName, mutableListOf(music))
            }
        }
        return artists.values.toList()
    }

    fun getMusicFromUri(context: Context, uri: Uri): Music? {
        var path = uri.path
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            context.contentResolver?.query(uri, projection, null, null, null)?.use {
                if (it.moveToFirst()) {
                    path = it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
                }
                it.close()
            }
        }
        return path?.let { getMusics(context, MediaStore.Audio.AudioColumns.DATA + " = ? ", it).firstOrNull() }
    }
}