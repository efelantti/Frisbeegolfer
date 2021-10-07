package fi.efelantti.frisbeegolfer

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DoubleExtensionsTests {

    @Test
    fun toPrettyStringReturnsCorrectResult() {
        val testValues =
            listOf<Float>(1.0f, 1.1f, 1.11f, 1.111f, 1.35f, 1.33333333333f, 10.0f, 10.23f)
        val expectedResults = listOf("1", "1,1", "1,1", "1,1", "1,4", "1,3", "10", "10,2")

        testValues.forEachIndexed { index, value ->
            assertThat(value.toPrettyString(), equalTo(expectedResults[index]))
        }
    }
}
