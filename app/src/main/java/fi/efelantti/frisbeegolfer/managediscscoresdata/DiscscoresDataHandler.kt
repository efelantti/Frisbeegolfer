package fi.efelantti.frisbeegolfer.managediscscoresdata

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat.DiscscoresCourses
import fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat.DiscscoresGames
import fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat.DiscscoresPlayer
import fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat.DiscscoresPlayers
import fi.efelantti.frisbeegolfer.model.Player
import java.util.*

class DiscscoresDataHandler(
    private val playersJson: String,
    private val coursesJson: String,
    private val gamesJson: String
) {
    private val moshi: Moshi = Moshi.Builder().build()
    private val discscoresPlayers: DiscscoresPlayers
    private val discscoresCourses: DiscscoresCourses
    private val discscoresGames: DiscscoresGames

    init {
        val playersAdapter: JsonAdapter<DiscscoresPlayers> =
            moshi.adapter(DiscscoresPlayers::class.java)
        val coursesAdapter: JsonAdapter<DiscscoresCourses> =
            moshi.adapter(DiscscoresCourses::class.java)
        val gamesAdapter: JsonAdapter<DiscscoresGames> = moshi.adapter(DiscscoresGames::class.java)
        discscoresPlayers = playersAdapter.fromJson(playersJson)
            ?: throw IllegalArgumentException("Error in deserializing players JSON.")
        discscoresCourses = coursesAdapter.fromJson(coursesJson)
            ?: throw IllegalArgumentException("Error in deserializing courses JSON.")
        discscoresGames = gamesAdapter.fromJson(gamesJson)
            ?: throw IllegalArgumentException("Error in deserializing games JSON.")
    }

    fun getPlayers(playersJson: String): List<Player> {
        return emptyList()
    }

    fun getPlayer(discscoresPlayer: DiscscoresPlayer): Player {
        // One way to get Long from UUID. However better maybe to use other methods to generate - for example, just use sequential longs.
        val uuid = UUID.fromString(discscoresPlayer.uuid)
        return Player(
            id = uuid.mostSignificantBits and Long.MAX_VALUE,
            name = discscoresPlayer.name
        )
    }
}