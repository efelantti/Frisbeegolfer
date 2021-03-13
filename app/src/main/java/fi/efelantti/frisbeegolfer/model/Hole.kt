package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
class Hole(
    @PrimaryKey(autoGenerate = true)
    var holeId: Long = 0,
    var parentCourseId: Long = 0,
    var holeNumber: Int = 0,
    var par: Int = 3,
    var lengthMeters: Int? = 0
): Parcelable
{
    companion object {
        fun equals(holes1: List<Hole>, holes2: List<Hole>): Boolean
        {
            if(holes1.count() != holes2.count()) return false
            else
            {
                for (i: Int in 0 until holes1.count())
                {
                    if(!Hole.equals(holes1[i], holes2[i])) return false
                }
            }
            return true
        }

        fun equals(hole1: Hole, hole2: Hole): Boolean
        {
            return hole1.lengthMeters == hole2.lengthMeters && hole1.par == hole2.par && hole1.holeNumber == hole2.holeNumber
        }
}
}

fun Hole.clone(): Hole {
    val hole = Hole()
    hole.holeId = holeId
    hole.parentCourseId = parentCourseId
    hole.holeNumber = holeNumber
    hole.par = par
    hole.lengthMeters = lengthMeters
    return hole
}