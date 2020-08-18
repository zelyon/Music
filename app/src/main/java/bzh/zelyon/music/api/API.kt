package bzh.zelyon.music.api

import bzh.zelyon.music.BuildConfig
import bzh.zelyon.music.api.model.AlbumResponse
import bzh.zelyon.music.api.model.ArtistResponse
import bzh.zelyon.music.api.model.MusicResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    companion object {
        val API = Retrofit.Builder()
            .baseUrl(BuildConfig.LAST_FM_API_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(OkHttpClient.Builder().build())
            .build()
            .create(API::class.java)
    }

    interface CallBack<T>: Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                response.body()?.let {
                    onSuccess(it)
                } ?: onFail(CallError(call, response))
            } else {
                onFail(CallError(call, response))
            }
        }

        override fun onFailure(call: Call<T>, throwable: Throwable) {
            onFail(CallError(call, throwable = throwable))
        }

        fun onSuccess(response: T)

        fun onFail(callError: CallError<T>) {}
    }

    class CallError<T>(call: Call<T>, response: Response<T>? = null, throwable: Throwable? = null) {

        var httpCode = 0
        var message = ""
        var method = ""
        var url = ""

        init {
            method = call.request().method
            url = call.request().url.toUrl().toString()

            response?.let {
                httpCode = response.code()
                message = response.message()
                response.errorBody()?.string()
            }
            throwable?.let {
                message = throwable.localizedMessage ?: throwable.message.orEmpty()
            }
        }
    }

    @GET("?method=artist.getInfo&format=json&api_key=${BuildConfig.LAST_FM_API_KEY}")
    fun getArtist(
        @Query("artist") artist: String): Call<ArtistResponse>

    @GET("?method=album.getInfo&format=json&api_key=${BuildConfig.LAST_FM_API_KEY}")
    fun getAlbum(
        @Query("artist") artist: String,
        @Query("album") album: String): Call<AlbumResponse>

    @GET("?method=track.getInfo&format=json&api_key=${BuildConfig.LAST_FM_API_KEY}")
    fun getMusic(
        @Query("artist") artist: String,
        @Query("track") album: String): Call<MusicResponse>
}