package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity
@Parcelize
@JsonClass(generateAdapter = true)
class Player(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String? = "",
    var email: String? = ""
) : Parcelable {
    override fun equals(other: Any?): Boolean = (other is Player)
            && id == other.id
            && name == other.name
            && email == other.email

    override fun hashCode() = Objects.hash(id, name, email)

    fun hasEmail(): Boolean {
        return email.isNullOrBlank()
    }

    enum class PlayerType(val id: Int) {
        PlayerWithEmail(id = 0),
        PlayerWithoutEmail(id = 1)
    }

    companion object {
        fun equals(player1: Player, player2: Player): Boolean {
            return player1.name == player2.name && player1.email == player2.email
        }
    }
}