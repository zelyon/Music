package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Artist(
    @SerializedName("name")
    @Expose
    var name: String,
    @SerializedName("bio")
    @Expose
    var bio: Wiki
)