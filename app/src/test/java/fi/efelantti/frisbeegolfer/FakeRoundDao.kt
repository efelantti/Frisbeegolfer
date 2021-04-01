package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.HoleStatistics
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
import java.time.OffsetDateTime

class FakeRoundDao: RoundDao {
    override suspend fun insert(round: Round) {
        TODO("Not yet implemented")
    }

    override suspend fun insert(score: Score): Long {
        TODO("Not yet implemented")
    }

    override suspend fun delete(round: Round) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(score: Score) {
        TODO("Not yet implemented")
    }

    override fun update(score: Score) {
        TODO("Not yet implemented")
    }

    override fun getRounds(): LiveData<List<RoundWithCourseAndScores>> {
        TODO("Not yet implemented")
    }

    override fun getRoundWithId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores> {
        TODO("Not yet implemented")
    }

    override fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics> {
        TODO("Not yet implemented")
    }
}