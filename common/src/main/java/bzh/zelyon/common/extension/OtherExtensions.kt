package bzh.zelyon.common.extension

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide

fun Int.millisecondsToDuration(): String {
    val nbHour = this/1000/60/60
    val nbMinutes = this/1000/60 - 60*nbHour
    val nbSecond = this/1000 - 60*60*nbHour - 60*nbMinutes
    return listOf(nbHour, nbMinutes, nbSecond).filterIndexed { index, it ->
        index != 0 || it > 0
    }.joinToString(separator = ":") { if (it < 10) "0$it" else "$it" }
}

fun ImageView.setImage(model: Any, placeholder: Drawable? = null) {
    Glide.with(this).load(model).apply {
        placeholder?.let { placeholder ->
                placeholder(placeholder)
                error(placeholder)
        }
    }.into(this)
}

