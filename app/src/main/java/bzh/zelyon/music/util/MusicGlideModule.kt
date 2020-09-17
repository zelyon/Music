package bzh.zelyon.music.util

import android.content.Context
import bzh.zelyon.lib.util.glide.AbsDataFetcher
import bzh.zelyon.lib.util.glide.AbsModelLoader
import bzh.zelyon.lib.util.glide.AbsModelLoaderFactory
import bzh.zelyon.music.db.model.Artist
import bzh.zelyon.music.db.model.Music
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

@GlideModule
class MusicGlideModule: AppGlideModule() {

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
    }
}