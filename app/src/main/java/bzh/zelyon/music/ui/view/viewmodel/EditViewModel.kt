package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bzh.zelyon.lib.util.api.CallBack
import bzh.zelyon.music.api.API
import bzh.zelyon.music.api.model.ArtistResponse
import bzh.zelyon.music.api.model.MusicResponse

class EditViewModel: ViewModel() {

    fun getArtist(artistName: String, french: Boolean = true): MutableLiveData<ArtistResponse> {
        val mutableLiveData = MutableLiveData<ArtistResponse>()
        API.getArtist(artistName, french).enqueue(object : CallBack<ArtistResponse> {
            override fun onSuccess(response: ArtistResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }

    fun getMusic(artistName: String, title: String, french: Boolean = true): MutableLiveData<MusicResponse> {
        val mutableLiveData = MutableLiveData<MusicResponse>()
        API.getMusic(artistName, title, french).enqueue(object : CallBack<MusicResponse> {
            override fun onSuccess(response: MusicResponse) {
                mutableLiveData.value = response
            }
        })
        return mutableLiveData
    }
}