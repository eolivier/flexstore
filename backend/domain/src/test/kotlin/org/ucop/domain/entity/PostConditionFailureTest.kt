package org.ucop.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.PostConditionException

class PostConditionFailureTest {

    private class WordNotSpelledException(errorMessage: ErrorMessage) : PostConditionException(errorMessage)
    private class WordLostException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)

    private val notSpelledMessage = ErrorMessage("The word was not spelled.")

    // A use case that spells a word, then checks with the given postcondition that it was spelled
    private class SpellWordUseCase(
        private val wordIsSpelled: PostCondition<String>,
        private val spelled: MutableList<String>
    ) : UseCase<String> {
        override fun getPreConditions(): PreConditions<String> = PreConditions.none()
        override fun getNominalScenario() = NominalScenario(Step<String> { word -> spelled.add(word) })
        override fun getPostConditions() = PostConditions(wordIsSpelled)
    }

    private class SpellWordQuery(
        private val answerIsSpelled: PostCondition<String>
    ) : QueryUseCase<String, String> {
        override fun getPreConditions(): PreConditions<String> = PreConditions.none()
        override fun getNominalQuery() = NominalQuery<String, String> { word -> word.uppercase() }
        override fun getPostConditions() = PostConditions(answerIsSpelled)
    }

    private val nothingIsSpelled = PostCondition<String> { throw WordNotSpelledException(notSpelledMessage) }

    @Test
    fun `a postcondition not met should fail the use case nominally, caused by the precise exception`() {
        // Act
        val thrown = catchThrowable { SpellWordUseCase(nothingIsSpelled, mutableListOf()).unfold("lamp") }
        // Assert
        assertThat(thrown.javaClass).isEqualTo(NominalException::class.java)
        assertThat(thrown).hasCauseInstanceOf(WordNotSpelledException::class.java)
        assertThat(thrown).hasMessage(notSpelledMessage.value)
    }

    @Test
    fun `a postcondition not met should fail the query nominally, caused by the precise exception`() {
        // Act
        val thrown = catchThrowable { SpellWordQuery(nothingIsSpelled).unfold("lamp") }
        // Assert
        assertThat(thrown.javaClass).isEqualTo(NominalException::class.java)
        assertThat(thrown).hasCauseInstanceOf(WordNotSpelledException::class.java)
    }

    @Test
    fun `a nominal exception raised by a postcondition should be thrown as it is`() {
        // Arrange
        val wordIsKept = PostCondition<String> { throw WordLostException(ErrorMessage("Word lost.")) }
        // Act
        val thrown = catchThrowable { SpellWordUseCase(wordIsKept, mutableListOf()).unfold("lamp") }
        // Assert
        assertThat(thrown).isInstanceOf(WordLostException::class.java)
        assertThat(thrown).hasNoCause()
    }

    @Test
    fun `a postcondition met should let the use case unfold`() {
        // Arrange
        val spelled = mutableListOf<String>()
        val wordIsSpelled = PostCondition<String> { word -> check(spelled.contains(word)) }
        // Act
        SpellWordUseCase(wordIsSpelled, spelled).unfold("lamp")
        // Assert
        assertThat(spelled).containsExactly("lamp")
    }
}
