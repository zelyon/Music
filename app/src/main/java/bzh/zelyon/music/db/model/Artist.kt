package bzh.zelyon.music.db.model

import bzh.zelyon.music.R

data class Artist(
    val id: Int,
    val name: String,
    val musics: MutableList<Music>): AbsModel() {

    override fun getDeclaration() = name

    override fun getPlaceholderId() = R.drawable.ic_artist

    override fun getTransitionName() = id.toString()
}
