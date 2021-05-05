package fi.efelantti.frisbeegolfer

import android.graphics.Point
import android.view.View

fun View.getLocationOnScreen(): Point {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return Point(location[0], location[1])
}