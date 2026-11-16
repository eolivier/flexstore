package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.order.model.ShippingMethodId
import org.flexstore.domain.order.usecase.ShippingMethodUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.rest.json.JsonNewShippingMethod
import org.flexstore.infra.spring.adapter.rest.json.JsonShippingMethod
import org.flexstore.infra.spring.adapter.rest.json.JsonShippingMethodChange
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The shipping methods, managed by the administrators (ADMIN role, see the security configuration, checked again by
// the use cases)
@RestController
@RequestMapping("/api/admin/shipping-methods")
class AdminShippingMethodController(
    private val shippingMethodUseCases: ShippingMethodUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getShippingMethods(@AuthenticationPrincipal reader: Jwt): List<JsonShippingMethod> {
        val actor = actorResolutionService.actorOf(reader)
        return shippingMethodUseCases.getAll(actor).asList().map { JsonShippingMethod(it) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createShippingMethod(
        @AuthenticationPrincipal creator: Jwt,
        @RequestBody newShippingMethod: JsonNewShippingMethod
    ): JsonShippingMethod {
        val shippingMethod = newShippingMethod.toShippingMethod()
        val actor = actorResolutionService.actorOf(creator)
        shippingMethodUseCases.create(actor, shippingMethod)
        return JsonShippingMethod(shippingMethod)
    }

    @PutMapping("/{id}")
    fun updateShippingMethod(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody shippingMethodChange: JsonShippingMethodChange
    ): JsonShippingMethod {
        val shippingMethod = shippingMethodChange.toShippingMethod(ShippingMethodId(Identity(id)))
        val actor = actorResolutionService.actorOf(editor)
        shippingMethodUseCases.update(actor, shippingMethod)
        return JsonShippingMethod(shippingMethod)
    }
}
