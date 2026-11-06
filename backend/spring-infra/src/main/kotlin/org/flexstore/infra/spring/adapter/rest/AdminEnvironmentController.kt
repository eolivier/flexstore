package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.environment.EnvironmentReading
import org.flexstore.domain.environment.ReadEnvironmentUseCase
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonEnvironmentVariable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The environment variables of the back end, for the administrators (ADMIN role, see SecurityConfig, checked again
// by the use case): the secrets are never given, only whether they are defined
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/environment")
class AdminEnvironmentController(private val readEnvironmentUseCase: ReadEnvironmentUseCase) {

    @Operation(summary = "Retrieve the environment variables of the shop, the secrets only as defined or not")
    @GetMapping
    fun getEnvironment(@AuthenticationPrincipal reader: Jwt): List<JsonEnvironmentVariable> {
        val environmentReading = EnvironmentReading(UserId.of(reader.subject))
        return JsonEnvironmentVariable.listOf(readEnvironmentUseCase.unfold(environmentReading))
    }
}
