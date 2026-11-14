package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.order.model.ShippingMethodId
import org.flexstore.domain.order.usecase.ShippingMethodUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonNewShippingMethod
import org.flexstore.infra.spring.adapter.rest.json.JsonShippingMethod
import org.flexstore.infra.spring.adapter.rest.json.JsonShippingMethodChange
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The shipping methods, managed by the administrators (ADMIN role, see the security configuration, checked again by
// the use cases)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/shipping-methods")
class AdminShippingMethodController(
    private val shippingMethodUseCases: ShippingMethodUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Retrieve all the shipping methods, the disabled ones included, the cheapest first")
    @GetMapping
    fun getShippingMethods(@AuthenticationPrincipal reader: Jwt): List<JsonShippingMethod> {
        val actor = actorResolutionService.actorOf(UserId.of(reader.subject))
        return shippingMethodUseCases.getAll(actor).asList().map { JsonShippingMethod(it) }
    }

    @Operation(summary = "Add a shipping method: a name, a cost, and whether it is free from the threshold")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createShippingMethod(
        @AuthenticationPrincipal creator: Jwt,
        @RequestBody newShippingMethod: JsonNewShippingMethod
    ): JsonShippingMethod {
        val shippingMethod = newShippingMethod.toShippingMethod()
        val actor = actorResolutionService.actorOf(UserId.of(creator.subject))
        shippingMethodUseCases.create(actor, shippingMethod)
        return JsonShippingMethod(shippingMethod)
    }

    @Operation(
        summary = "Change a shipping method (its name, its cost, whether it is free from the threshold, whether " +
            "it is enabled): at least one method stays enabled, the orders already placed keep their shipping cost"
    )
    @PutMapping("/{id}")
    fun updateShippingMethod(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody shippingMethodChange: JsonShippingMethodChange
    ): JsonShippingMethod {
        val shippingMethod = shippingMethodChange.toShippingMethod(ShippingMethodId(Identity(id)))
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        shippingMethodUseCases.update(actor, shippingMethod)
        return JsonShippingMethod(shippingMethod)
    }
}
