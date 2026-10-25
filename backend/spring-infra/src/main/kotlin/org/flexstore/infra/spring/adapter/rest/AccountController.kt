package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.email.VerificationToken
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
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

// The accounts the customers create for themselves (see SecurityConfig: public, except the new link)
@Tag(name = "Accounts")
@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val customerRegistrationService: CustomerRegistrationService,
    private val addressBookService: AddressBookService
) {

    @Operation(summary = "Create the account of a customer and send them a verification link, valid for 1 hour")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody registration: JsonAccountRegistration): JsonUser =
        customerRegistrationService.register(registration.toRegistration(UserId.nextId())).toJsonUser()

    @Operation(summary = "Retrieve the account of the signed-in user, with their phone number and postal addresses")
    @GetMapping("/me")
    fun getAccount(@AuthenticationPrincipal user: Jwt): JsonAccount =
        JsonAccount(customerRegistrationService.getAccount(UserId.of(user.subject)))

    @Operation(summary = "Retrieve the postal addresses of the signed-in user, in their order")
    @GetMapping("/me/addresses")
    fun getAddresses(@AuthenticationPrincipal user: Jwt): List<JsonPostalAddress> =
        addressBookService.getAddresses(UserId.of(user.subject)).toJson()

    @Operation(summary = "Change an address of the signed-in user (0 for the first one); the orders keep theirs")
    @PutMapping("/me/addresses/{position}")
    fun changeAddress(
        @AuthenticationPrincipal user: Jwt,
        @PathVariable position: Int,
        @RequestBody address: JsonPostalAddress
    ): List<JsonPostalAddress> {
        val userAddress = UserAddress(UserId.of(user.subject), AddressPosition(position))
        val addressChange = AddressChange(userAddress, address.toPostalAddress())
        return addressBookService.change(addressChange).toJson()
    }

    @Operation(summary = "Remove an address of the signed-in user (0 for the first one); the orders keep theirs")
    @DeleteMapping("/me/addresses/{position}")
    fun removeAddress(@AuthenticationPrincipal user: Jwt, @PathVariable position: Int): List<JsonPostalAddress> {
        val userAddress = UserAddress(UserId.of(user.subject), AddressPosition(position))
        return addressBookService.remove(userAddress).toJson()
    }

    private fun PostalAddresses.toJson() = asList().map { JsonPostalAddress.of(it) }

    @Operation(summary = "Verify the email of an account with the token of the link: the account becomes active")
    @PostMapping("/email-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verifyEmail(@RequestBody verification: JsonEmailVerification) =
        customerRegistrationService.verifyEmail(VerificationToken(verification.token))

    @Operation(summary = "Send a new verification link to the signed-in customer; the previous links stop working")
    @PostMapping("/me/verification-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun sendVerificationEmail(@AuthenticationPrincipal customer: Jwt) =
        customerRegistrationService.sendVerificationEmail(UserId.of(customer.subject))
}
