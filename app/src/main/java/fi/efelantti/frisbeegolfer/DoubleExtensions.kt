package fi.efelantti.frisbeegolfer

import kotlin.math.abs

fun Float.toPrettyString() =
    if ((abs(this).rem(1.0) < 1e-10))
        "%.0f".format(this)
    else
        "%.1f".format(this)
