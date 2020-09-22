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
        val instance: API = Retrofit.Builder()
            .baseUrl(BuildConfig.LAST_FM_API_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(OkHttpClient.Builder().build())
            .build()
            .create(API::class.java)
    }

    @GET("?method=artist.getInfo&lang=fr&format=json&api_key=${BuildConfig.LAST_FM_API_KEY}")
    fun getArtist(@Query("artist") artist: String): Call<ArtistResponse>

    @GET("?method=album.getInfo&lang=fr&format=json&api_key=${BuildConfig.LAST_FM_API_KEY}")
    fun getAlbum(@Query("artist") artist: String, @Query("album") album: String): Call<AlbumResponse>

    @GET("?method=track.getInfo&lang=fr&format=json&api_key=${BuildConfig.LAST_FM_API_KEY}")
    fun getMusic(@Query("artist") artist: String, @Query("track") album: String): Call<MusicResponse>
}