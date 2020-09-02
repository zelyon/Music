package bzh.zelyon.common.util.glide

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.InputStream

abstract class AbsDataFetcher: DataFetcher<InputStream> {
    var inputStream: InputStream? = null
    override fun getDataClass() = InputStream::class.java
    override fun getDataSource() = DataSource.LOCAL
    override fun cancel() {}
    override fun cleanup() { inputStream?.close() }
}