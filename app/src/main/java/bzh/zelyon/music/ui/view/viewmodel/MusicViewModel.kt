package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicViewModel: ViewModel() {

    var needReloadLibrary = MutableLiveData<Boolean>()
}