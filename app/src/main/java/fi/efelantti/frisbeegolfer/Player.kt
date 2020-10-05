package fi.efelantti.frisbeegolfer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String? = "",
    val nickName: String? = "",
    val lastName: String? = "",
    val email: String? = ""
)