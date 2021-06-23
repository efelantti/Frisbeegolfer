package fi.efelantti.frisbeegolfer

fun Float.toPrettyString() =
    if (this - this.toLong() == 0.0f)
        String.format("%d", this.toLong())
    else
        String.format("%s", this)