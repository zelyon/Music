package bzh.zelyon.music.db.model

import java.io.Serializable

abstract class AbsModel : Serializable {

    abstract fun getDeclaration(): String

    abstract fun getPlaceholderId(): Int

    abstract fun getTransitionName(): String
}