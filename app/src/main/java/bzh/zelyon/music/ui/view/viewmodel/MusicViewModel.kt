package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicViewModel: ViewModel() {

    val needReloadLibrary = MutableLiveData<Any?>()
}