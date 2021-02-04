package bzh.zelyon.music.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import bzh.zelyon.music.db.model.Playlist

@Dao
interface PlaylistDao {

    @Insert
    fun insert(playlist: Playlist)

    @Delete
    fun delete(playlist: Playlist)

    @Update
    fun update(playlist: Playlist)

    @Query("SELECT * FROM playlist ORDER BY name")
    fun getAll(): List<Playlist>

    @Query("SELECT * FROM playlist ORDER BY name")
    fun getAllLiveData(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlist WHERE musics != '[]' ORDER BY name")
    fun getNotEmptyLiveData(): LiveData<List<Playlist>>
}