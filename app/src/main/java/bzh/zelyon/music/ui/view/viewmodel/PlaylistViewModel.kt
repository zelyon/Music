package bzh.zelyon.music.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import bzh.zelyon.music.db.DB

class PlaylistViewModel: ViewModel() {

    val playlists = DB.getPlaylistDao().getAll()

    val playlistsNotEmpty = DB.getPlaylistDao().getNotEmpty()
}