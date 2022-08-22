package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Image(
    @SerializedName("#text")
    @Expose
    var text: String
)