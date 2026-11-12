package org.ucop.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.PreConditionException

class PreConditionFailureTest {

    private class EmptyWordException(errorMessage: ErrorMessage) : PreConditionException(errorMessage)
    private class UnknownWordException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)

    private val emptyWordMessage = ErrorMessage("The word is empty.")

    // A use case that checks the word with the given precondition, and counts the words it spelled
    private class SpellWordUseCase(
        private val wordIsChecked: PreCondition<String>,
        private val spelled: MutableList<String>
    ) : UseCase<String> {
        override fun getPreConditions() = PreConditions(wordIsChecked)
        override fun getNominalScenario() = NominalScenario(Step<String> { word -> spelled.add(word) })
        override fun getPostConditions(): PostConditions<String> = PostConditions.none()
    }

    private class SpellWordQuery(
        private val wordIsChecked: PreCondition<String>
    ) : QueryUseCase<String, String> {
        override fun getPreConditions() = PreConditions(wordIsChecked)
        override fun getNominalQuery() = NominalQuery<String, String> { word -> word.uppercase() }
        override fun getPostConditions(): PostConditions<String> = PostConditions.none()
    }

    private val wordIsNotEmpty = PreCondition<String> { word ->
        if (word.isEmpty()) throw EmptyWordException(emptyWordMessage)
    }

    @Test
    fun `a precondition not met should fail the use case nominally, caused by the precise exception`() {
        // Arrange
        val spelled = mutableListOf<String>()
        // Act
        val thrown = catchThrowable { SpellWordUseCase(wordIsNotEmpty, spelled).unfold("") }
        // Assert
        assertThat(thrown.javaClass).isEqualTo(NominalException::class.java)
        assertThat(thrown).hasCauseInstanceOf(EmptyWordException::class.java)
        assertThat(thrown).hasMessage(emptyWordMessage.value)
        assertThat(spelled).isEmpty()
    }

    @Test
    fun `a precondition not met should fail the query nominally, caused by the precise exception`() {
        // Act
        val thrown = catchThrowable { SpellWordQuery(wordIsNotEmpty).unfold("") }
        // Assert
        assertThat(thrown.javaClass).isEqualTo(NominalException::class.java)
        assertThat(thrown).hasCauseInstanceOf(EmptyWordException::class.java)
    }

    @Test
    fun `a nominal exception raised by a precondition should be thrown as it is`() {
        // Arrange
        val wordIsKnown = PreCondition<String> { throw UnknownWordException(ErrorMessage("Unknown word.")) }
        // Act
        val thrown = catchThrowable { SpellWordUseCase(wordIsKnown, mutableListOf()).unfold("lamp") }
        // Assert
        assertThat(thrown).isInstanceOf(UnknownWordException::class.java)
        assertThat(thrown).hasNoCause()
    }

    @Test
    fun `a precondition met should let the use case unfold`() {
        // Arrange
        val spelled = mutableListOf<String>()
        // Act
        SpellWordUseCase(wordIsNotEmpty, spelled).unfold("lamp")
        // Assert
        assertThat(spelled).containsExactly("lamp")
    }
}
