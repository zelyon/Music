package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bzh.zelyon.lib.ui.view.fragment.AbsFragment
import bzh.zelyon.music.ui.view.fragment.main.PlayingFragment

class MainViewModel: ViewModel() {

    enum class FABState { ANIM_PLAY, ANIM_PAUSE, ICON_PLAY, ICON_PAUSE, HIDE }

    val currentFragment = MutableLiveData<AbsFragment?>(null)

    val isPlaying = MutableLiveData(false)

    val hasPlayingList = MutableLiveData(false)

    val fabState = MediatorLiveData<FABState>()

    init {
        fabState.addSource(currentFragment) {
            fabState.value = combineLiveDatas(currentFragment, isPlaying, hasPlayingList)
        }
        fabState.addSource(isPlaying) {
            fabState.value = combineLiveDatas(currentFragment, isPlaying, hasPlayingList)
        }
        fabState.addSource(hasPlayingList) {
            fabState.value = combineLiveDatas(currentFragment, isPlaying, hasPlayingList)
        }
    }

    private fun combineLiveDatas(currentFragment: MutableLiveData<AbsFragment?>, isPlaying: MutableLiveData<Boolean>, hasPlayingList: MutableLiveData<Boolean>) : FABState {
        return if (hasPlayingList.value == true) {
            if (currentFragment.value is PlayingFragment) {
                if (isPlaying.value == true) {
                    FABState.ICON_PLAY
                } else {
                    FABState.ICON_PAUSE
                }
            } else {
                if (isPlaying.value == true) {
                    FABState.ANIM_PLAY
                } else {
                    FABState.ANIM_PAUSE
                }
            }
        } else {
            FABState.HIDE
        }
    }
}