package bzh.zelyon.common.util.glide

import com.bumptech.glide.load.model.ModelLoader
import java.io.InputStream

abstract class AbsModelLoader<T>: ModelLoader<T, InputStream> {
    override fun handles(model: T) = true
}