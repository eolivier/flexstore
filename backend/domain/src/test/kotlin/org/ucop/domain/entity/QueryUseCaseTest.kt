package org.ucop.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class QueryUseCaseTest {

    // Answers the length of a word, which must not be empty, and checks that the answer is positive
    private class WordLengthQuery(private val checked: MutableList<String>) : QueryUseCase<String, Int> {

        override fun getPreConditions() = PreConditions(PreCondition<String> { word ->
            checked.add("precondition $word")
            require(word.isNotEmpty()) { "empty word" }
        })

        override fun getNominalQuery() = NominalQuery<String, Int> { word ->
            checked.add("query $word")
            word.length
        }

        override fun getPostConditions() = PostConditions(PostCondition<Int> { length ->
            checked.add("postcondition $length")
            check(length > 0) { "no length" }
        })
    }

    @Test
    fun `should answer once the preconditions are checked, then check the answer`() {
        // Arrange
        val checked = mutableListOf<String>()
        val wordLengthQuery = WordLengthQuery(checked)
        // Act
        val answer = wordLengthQuery.unfold("lamp")
        // Assert
        assertThat(answer).isEqualTo(4)
        assertThat(checked).containsExactly("precondition lamp", "query lamp", "postcondition 4")
    }

    @Test
    fun `should not answer when a precondition fails`() {
        // Arrange
        val checked = mutableListOf<String>()
        val wordLengthQuery = WordLengthQuery(checked)
        // Act
        val thrown = catchThrowable { wordLengthQuery.unfold("") }
        // Assert
        assertThat(thrown).hasMessage("empty word")
        assertThat(checked).containsExactly("precondition ")
    }
}
