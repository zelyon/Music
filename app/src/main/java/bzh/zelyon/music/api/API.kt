package bzh.zelyon.music.api

import bzh.zelyon.music.BuildConfig
import bzh.zelyon.music.api.model.AlbumResponse
import bzh.zelyon.music.api.model.ArtistResponse
import bzh.zelyon.music.api.model.MusicResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    companion object {
        private val instance: API = Retrofit.Builder()
            .baseUrl(BuildConfig.LAST_FM_API_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(OkHttpClient.Builder().build())
            .build()
            .create(API::class.java)

        fun getArtist(artist: String, french: Boolean = true) = instance.getArtist(
            artist,
            if (french) "fr" else "en",
            "json",
            BuildConfig.LAST_FM_API_KEY)

        fun getAlbum(artist: String, album: String, french: Boolean = true) = instance.getAlbum(
            artist,
            album,
            if (french) "fr" else "en",
            "json",
            BuildConfig.LAST_FM_API_KEY)

        fun getMusic(artist: String, title: String, french: Boolean = true) = instance.getMusic(
            artist,
            title,
            if (french) "fr" else "en",
            "json",
            BuildConfig.LAST_FM_API_KEY
        )
    }

    @GET("?method=artist.getInfo")
    fun getArtist(
        @Query("artist") artist: String,
        @Query("lang") lang: String,
        @Query("format") format: String,
        @Query("api_key") api_key: String): Call<ArtistResponse>

    @GET("?method=album.getInfo")
    fun getAlbum(
        @Query("artist") artist: String,
        @Query("album") album: String,
        @Query("lang") lang: String,
        @Query("format") format: String,
        @Query("api_key") api_key: String): Call<AlbumResponse>

    @GET("?method=track.getInfo")
    fun getMusic(
        @Query("artist") artist: String,
        @Query("track") track: String,
        @Query("lang") lang: String,
        @Query("format") format: String,
        @Query("api_key") api_key: String): Call<MusicResponse>
}