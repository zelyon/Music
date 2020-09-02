package bzh.zelyon.common.util.glide

import com.bumptech.glide.load.model.ModelLoaderFactory
import java.io.InputStream

abstract class AbsModelLoaderFactory<T>: ModelLoaderFactory<T, InputStream> {
    override fun teardown() {}
}