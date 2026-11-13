package org.flexstore.domain.actor

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.systemUser
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class ActorResolutionServiceSystemTest : WithAssertions {

    private val service = ActorResolutionService(InMemoryUserRepository().apply { save(systemUser) })

    @Test
    fun `the user with the SYSTEM role should be the system actor`() {
        // Act
        val actor = service.system()
        // Assert
        assertThat(actor.isSystem()).isTrue()
    }
}
