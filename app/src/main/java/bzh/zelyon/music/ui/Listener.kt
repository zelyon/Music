package bzh.zelyon.music.ui

import java.io.Serializable

interface Listener : Serializable {
    fun needToReload()
}