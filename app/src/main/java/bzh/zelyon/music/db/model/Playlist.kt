package bzh.zelyon.music.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import bzh.zelyon.music.db.MusicsConverter
import java.io.Serializable


@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int?,
    @ColumnInfo(name = "name")
    var name: String): Serializable {
    @ColumnInfo(name = "musics")
    @TypeConverters(MusicsConverter::class)
    var musics = mutableListOf<Music>()
}