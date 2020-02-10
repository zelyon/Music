package bzh.zelyon.music.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Album(
    @SerializedName("name") @Expose var name: String,
    @SerializedName("title") @Expose var title: String,
    @SerializedName("image") @Expose var image: List<Image>,
    @SerializedName("wiki") @Expose var wiki: Wiki
)