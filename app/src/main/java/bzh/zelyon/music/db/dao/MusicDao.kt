package bzh.zelyon.music.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import bzh.zelyon.music.db.model.Music

@Dao
interface MusicDao {

    @Insert
    fun insert(music: List<Music>)

    @Query("SELECT * FROM music")
    fun getAll(): List<Music>

    @Query("DELETE FROM music")
    fun deleteAll()
}