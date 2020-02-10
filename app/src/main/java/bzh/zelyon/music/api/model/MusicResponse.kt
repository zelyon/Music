package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MusicResponse(@SerializedName("track") @Expose var track: Music)