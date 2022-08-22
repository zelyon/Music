package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Wiki(
    @SerializedName("content")
    @Expose
    var content: String
)