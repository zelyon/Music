package bzh.zelyon.music.db

import androidx.room.TypeConverter
import bzh.zelyon.music.db.model.Music
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MusicsConverter {

    @TypeConverter
    fun fromMusicList(musics: List<Music>?): String = Gson().toJson(musics)

    @TypeConverter
    fun toMusicList(musicListString: String?): List<Music> = Gson().fromJson<List<Music>>(musicListString, object : TypeToken<List<Music?>?>() {}.type)
}