package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
class Player(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var firstName: String? = "",
    val nickName: String? = "",
    val lastName: String? = "",
    val email: String? = ""
) : Parcelable
{
    companion object {
        fun equals(player1: Player, player2: Player): Boolean
        {
            return player1.firstName == player2.firstName && player1.nickName == player2.nickName && player1.lastName == player2.lastName && player1.email == player2.email
        }
    }
}