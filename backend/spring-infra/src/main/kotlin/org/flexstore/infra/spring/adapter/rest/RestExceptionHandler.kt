package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.ucop.domain.NominalException

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(NominalException::class)
    fun handleNominalException(ex: NominalException): ResponseEntity<Map<String, String>> {
        return when (ex) {
            is UserAlreadyExists -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "User already exists")))
            // Same answer for an unknown email and a wrong password, so that the login does not reveal which accounts exist
            is InvalidCredentialsException, is UserNotFoundByEmailException -> ResponseEntity
                .status(401)
                .body(mapOf("error" to "Invalid credentials."))
            // The password is right, but the account is suspended, locked or disabled: the front tells the user why
            is AccountSignInRefusedException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Sign-in refused"), "status" to ex.status.name))
            is OrderRefusedException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Order refused"), "status" to ex.status.name))
            is UserCreationForbiddenException, is UserUpdateForbiddenException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Forbidden")))
            // Not 422, which tells the front that the cart is empty
            is IncompleteOrderAddressException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Incomplete address")))
            is InvalidCurrentPasswordException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Wrong current password")))
            is ProductNotFoundException, is OrderNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Not found")))
            is VerificationLinkUnknownException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Unknown verification link")))
            // The link was right but is too old: the customer can ask for a new one
            is VerificationLinkExpiredException -> ResponseEntity
                .status(410)
                .body(mapOf("error" to (ex.message ?: "Expired verification link")))
            is AccountAlreadyVerifiedException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Email already verified")))
            else -> ResponseEntity
                .status(422)
                .body(mapOf("error" to (ex.message ?: "Nominal exception")))
        }
    }
}