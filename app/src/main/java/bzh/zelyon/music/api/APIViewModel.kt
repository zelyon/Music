package bzh.zelyon.music.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bzh.zelyon.common.util.api.CallBack
import bzh.zelyon.music.api.model.AlbumResponse
import bzh.zelyon.music.api.model.ArtistResponse
import bzh.zelyon.music.api.model.MusicResponse

class APIViewModel: ViewModel() {

    fun getArtist(artistName: String): MutableLiveData<ArtistResponse> {
        val mutableLiveData = MutableLiveData<ArtistResponse>()
        API.API.getArtist(artistName).enqueue(object : CallBack<ArtistResponse> {
            override fun onSuccess(response: ArtistResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }

    fun getAlbum(artistName: String, albumName: String): MutableLiveData<AlbumResponse> {
        val mutableLiveData = MutableLiveData<AlbumResponse>()
        API.API.getAlbum(artistName, albumName).enqueue(object : CallBack<AlbumResponse> {
            override fun onSuccess(response: AlbumResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }

    fun getMusic(artistName: String, title: String): MutableLiveData<MusicResponse> {
        val mutableLiveData = MutableLiveData<MusicResponse>()
        API.API.getMusic(artistName, title).enqueue(object : CallBack<MusicResponse> {
            override fun onSuccess(response: MusicResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }
}