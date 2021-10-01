package fi.efelantti.frisbeegolfer.model

data class HoleStatistics(
    var bestResult: Int? = -1,
    var avgResult: Float? = -1f,
    var latestResult: Int? = -1
)