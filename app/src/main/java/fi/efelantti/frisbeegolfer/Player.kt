package fi.efelantti.frisbeegolfer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val firstName: String?,
    val lastName: String?,
    val nickName: String?,
    val email: String?
)