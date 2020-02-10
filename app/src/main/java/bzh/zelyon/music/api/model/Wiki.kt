package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Wiki(
    @SerializedName("summary") @Expose var summary: String,
    @SerializedName("content") @Expose var content: String
)