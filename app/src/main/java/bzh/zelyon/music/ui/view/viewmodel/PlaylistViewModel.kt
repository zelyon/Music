package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import bzh.zelyon.music.db.DB

class PlaylistViewModel: ViewModel() {

    val playlists = DB.getPlaylistDao().getAllLiveData()

    val playlistsNotEmpty = DB.getPlaylistDao().getNotEmptyLiveData()
}