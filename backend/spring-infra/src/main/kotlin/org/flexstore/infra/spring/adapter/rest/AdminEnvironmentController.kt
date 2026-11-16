package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.environment.EnvironmentReading
import org.flexstore.domain.environment.ReadEnvironmentUseCase
import org.flexstore.infra.spring.adapter.rest.json.JsonEnvironmentVariable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The environment variables of the back end, for the administrators (ADMIN role, see SecurityConfig, checked
// again by the use case): the secrets are never given, only whether they are defined
@RestController
@RequestMapping("/api/admin/environment")
class AdminEnvironmentController(
    private val readEnvironmentUseCase: ReadEnvironmentUseCase,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getEnvironment(@AuthenticationPrincipal reader: Jwt): List<JsonEnvironmentVariable> {
        val actor = actorResolutionService.actorOf(reader)
        val environmentReading = EnvironmentReading(actor)
        return JsonEnvironmentVariable.listOf(readEnvironmentUseCase.unfold(environmentReading))
    }
}
