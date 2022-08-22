package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Music(
    @SerializedName("name")
    @Expose
    var name: String,
    @SerializedName("artist")
    @Expose
    var artist: Artist,
    @SerializedName("album")
    @Expose
    var album: Album,
    @SerializedName("wiki")
    @Expose
    var wiki: Wiki
)