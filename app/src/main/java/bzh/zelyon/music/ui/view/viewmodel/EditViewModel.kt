package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bzh.zelyon.lib.util.api.CallBack
import bzh.zelyon.music.api.API
import bzh.zelyon.music.api.model.AlbumResponse
import bzh.zelyon.music.api.model.ArtistResponse
import bzh.zelyon.music.api.model.MusicResponse

class EditViewModel: ViewModel() {

    fun getArtist(artistName: String): MutableLiveData<ArtistResponse> {
        val mutableLiveData = MutableLiveData<ArtistResponse>()
        API.getArtist(artistName).enqueue(object : CallBack<ArtistResponse> {
            override fun onSuccess(response: ArtistResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }

    fun getAlbum(artistName: String, albumName: String): MutableLiveData<AlbumResponse> {
        val mutableLiveData = MutableLiveData<AlbumResponse>()
        API.getAlbum(artistName, albumName).enqueue(object : CallBack<AlbumResponse> {
            override fun onSuccess(response: AlbumResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }

    fun getMusic(artistName: String, title: String): MutableLiveData<MusicResponse> {
        val mutableLiveData = MutableLiveData<MusicResponse>()
        API.getMusic(artistName, title).enqueue(object : CallBack<MusicResponse> {
            override fun onSuccess(response: MusicResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }
}