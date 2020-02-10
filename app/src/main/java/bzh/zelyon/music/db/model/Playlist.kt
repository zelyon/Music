package bzh.zelyon.music.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import bzh.zelyon.music.R
import bzh.zelyon.music.db.MusicsConverter


@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int?,
    @ColumnInfo(name = "name")
    var name: String): AbsModel() {

    @ColumnInfo(name = "musics")
    @TypeConverters(MusicsConverter::class)
    var musics = mutableListOf<Music>()

    override fun getDeclaration() = name

    override fun getPlaceholderId() = R.drawable.ic_playlist

    override fun getTransitionName() = id.toString()
}