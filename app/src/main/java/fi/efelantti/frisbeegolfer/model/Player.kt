package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
class Player(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String? = "",
    val email: String? = ""
) : Parcelable {
    override fun equals(other: Any?): Boolean = (other is Player)
            && id == other.id
            && name == other.name
            && email == other.email

    override fun hashCode() = Objects.hash(id, name, email)

    companion object {
        fun equals(player1: Player, player2: Player): Boolean {
            return player1.name == player2.name && player1.email == player2.email
        }
    }
}