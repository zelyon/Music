package bzh.zelyon.music.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import bzh.zelyon.music.db.dao.MusicDao
import bzh.zelyon.music.db.dao.PlaylistDao
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.db.model.Playlist

@Database(entities = [Playlist::class, Music::class], version = 1)
abstract class DB: RoomDatabase() {

    companion object {

        private lateinit var db: DB

        fun init(context: Context) {
            db = Room.databaseBuilder(context, DB::class.java, "music").allowMainThreadQueries().build()
        }

        fun getPlaylistDao() = db.getPlaylistDao()
        fun getMusicDao() = db.getMusicDao()
    }

    abstract fun getPlaylistDao(): PlaylistDao
    abstract fun getMusicDao(): MusicDao
}