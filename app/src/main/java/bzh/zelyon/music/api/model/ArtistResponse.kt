package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ArtistResponse(
    @SerializedName("artist")
    @Expose
    var artist: Artist
)