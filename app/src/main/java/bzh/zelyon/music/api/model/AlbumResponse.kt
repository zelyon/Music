package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AlbumResponse(@SerializedName("album") @Expose var album: Album)