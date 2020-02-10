package bzh.zelyon.music.utils

import android.content.Context
import bzh.zelyon.music.db.model.Artist
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.db.model.Playlist
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

@GlideModule
class MusicModule: AppGlideModule() {

    abstract class AbsModelLoaderFactory<T>: ModelLoaderFactory<T, InputStream> {
        override fun teardown() {}
    }

    abstract class AbsModelLoader<T>: ModelLoader<T, InputStream> {
        override fun handles(model: T) = true
    }

    abstract class AbsDataFetcher: DataFetcher<InputStream> {
        var inputStream: InputStream? = null
        override fun getDataClass() = InputStream::class.java
        override fun getDataSource() = DataSource.LOCAL
        override fun cancel() {}
        override fun cleanup() { inputStream?.close() }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        registry.prepend(Music::class.java, InputStream::class.java, object : AbsModelLoaderFactory<Music>() {
            override fun build(multiFactory: MultiModelLoaderFactory) = object : AbsModelLoader<Music>() {
                override fun buildLoadData(model: Music, width: Int, height: Int, options: Options) = ModelLoader.LoadData(ObjectKey(model), object : AbsDataFetcher() {
                    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
                        inputStream = model.getArtworkInputStreamFromPath(context)
                        inputStream?.let {
                            callback.onDataReady(inputStream)
                        }
                    }
                })
            }
        })
        registry.prepend(Artist::class.java, InputStream::class.java, object : AbsModelLoaderFactory<Artist>() {
            override fun build(multiFactory: MultiModelLoaderFactory) = object : AbsModelLoader<Artist>() {
                override fun buildLoadData(model: Artist, width: Int, height: Int, options: Options) = ModelLoader.LoadData(ObjectKey(model), object : AbsDataFetcher() {
                    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
                        var index = 0
                        while (inputStream == null && index < model.musics.size) {
                            inputStream = model.musics[index].getArtworkInputStreamFromPath(context)
                            index++
                        }
                        inputStream?.let {
                            callback.onDataReady(inputStream)
                        }
                    }
                })
            }
        })
        registry.prepend(Playlist::class.java, InputStream::class.java, object : AbsModelLoaderFactory<Playlist>() {
            override fun build(multiFactory: MultiModelLoaderFactory) = object : AbsModelLoader<Playlist>() {
                override fun buildLoadData(model: Playlist, width: Int, height: Int, options: Options) = ModelLoader.LoadData(ObjectKey(model), object : AbsDataFetcher() {
                    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
                        var index = 0
                        while (inputStream == null && index < model.musics.size) {
                            inputStream = model.musics[index].getArtworkInputStreamFromPath(context)
                            index++
                        }
                        inputStream?.let {
                            callback.onDataReady(inputStream)
                        }
                    }
                })
            }
        })
    }
}