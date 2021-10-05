package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LibraryViewModel: ViewModel() {

    val needReload = MutableLiveData<Boolean>()
}