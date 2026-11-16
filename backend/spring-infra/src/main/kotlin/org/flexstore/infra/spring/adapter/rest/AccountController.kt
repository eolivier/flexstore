package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.email.model.VerificationToken
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.service.*
import org.flexstore.infra.spring.adapter.mapping.toJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonAccount
import org.flexstore.infra.spring.adapter.rest.json.JsonAccountRegistration
import org.flexstore.infra.spring.adapter.rest.json.JsonEmailVerification
import org.flexstore.infra.spring.adapter.rest.json.JsonPostalAddress
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.flexstore.domain.user.address.AddressAddition
import org.flexstore.domain.user.address.AddressBookService
import org.flexstore.domain.user.address.AddressChange
import org.flexstore.domain.user.address.AddressPosition
import org.flexstore.domain.user.address.UserAddress

// The accounts the customers create for themselves (see SecurityConfig: public, except the new link)
@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val customerRegistrationService: CustomerRegistrationService,
    private val addressBookService: AddressBookService,
    private val actorResolutionService: ActorResolutionService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody registration: JsonAccountRegistration): JsonUser =
        customerRegistrationService.register(registration.toRegistration(UserId.nextId())).toJsonUser()

    @GetMapping("/me")
    fun getAccount(@AuthenticationPrincipal user: Jwt): JsonAccount =
        JsonAccount(customerRegistrationService.getAccount(UserId.of(user.subject)))

    @GetMapping("/me/addresses")
    fun getAddresses(@AuthenticationPrincipal user: Jwt): List<JsonPostalAddress> =
        addressBookService.getAddresses(actorResolutionService.actorOf(user)).toJson()

    @PostMapping("/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    fun addAddress(
        @AuthenticationPrincipal user: Jwt,
        @RequestBody address: JsonPostalAddress
    ): List<JsonPostalAddress> {
        val addressAddition = AddressAddition(actorResolutionService.actorOf(user), address.toPostalAddress())
        return addressBookService.add(addressAddition).toJson()
    }

    @PutMapping("/me/addresses/{position}")
    fun changeAddress(
        @AuthenticationPrincipal user: Jwt,
        @PathVariable position: Int,
        @RequestBody address: JsonPostalAddress
    ): List<JsonPostalAddress> {
        val userAddress = UserAddress(actorResolutionService.actorOf(user), AddressPosition(position))
        val addressChange = AddressChange(userAddress, address.toPostalAddress())
        return addressBookService.change(addressChange).toJson()
    }

    @DeleteMapping("/me/addresses/{position}")
    fun removeAddress(@AuthenticationPrincipal user: Jwt, @PathVariable position: Int): List<JsonPostalAddress> {
        val userAddress = UserAddress(actorResolutionService.actorOf(user), AddressPosition(position))
        return addressBookService.remove(userAddress).toJson()
    }

    private fun PostalAddresses.toJson() = asList().map { JsonPostalAddress.of(it) }

    @PostMapping("/email-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verifyEmail(@RequestBody verification: JsonEmailVerification) =
        customerRegistrationService.verifyEmail(VerificationToken(verification.token))

    @PostMapping("/me/verification-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun sendVerificationEmail(@AuthenticationPrincipal customer: Jwt) =
        customerRegistrationService.sendVerificationEmail(UserId.of(customer.subject))
}
