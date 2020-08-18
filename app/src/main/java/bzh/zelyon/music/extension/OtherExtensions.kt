package bzh.zelyon.music.extension

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

fun Int.millisecondstoDuration(): String = SimpleDateFormat(if (this < 60*60*1000) "mm:ss" else "hh:mm:ss")
    .format(Calendar.getInstance().apply {
        set(0, 0, 0, 0, 0, 0)
        set(Calendar.SECOND, toInt()/1000)
    }.time)

fun ImageView.setImage(model: Any, placeholder: Drawable? = null) {
    Glide.with(this).load(model).apply {
        placeholder?.let { placeholder ->
                placeholder(placeholder)
                error(placeholder)
        }
    }.into(this)
}

