package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.product.*
import org.flexstore.domain.image.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.ucop.domain.NominalException

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(NominalException::class)
    fun handleNominalException(ex: NominalException): ResponseEntity<Map<String, String>> {
        return when (ex) {
            is UserAlreadyExists -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "User already exists")))
            // Same answer for an unknown email and a wrong password, so that the login does not reveal which
            // accounts exist
            is InvalidCredentialsException, is UserNotFoundByEmailException -> ResponseEntity
                .status(401)
                .body(mapOf("error" to "Invalid credentials."))
            // The password is right, but the account is suspended, locked or disabled: the front tells the user
            // why
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
            is IncompleteAddressException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Incomplete address")))
            is ProductNotFoundException, is OrderNotFoundException, is AddressNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Not found")))
            is VerificationLinkUnknownException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Unknown verification link")))
            is PasswordResetLinkUnknownException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Unknown link to choose a new password")))
            // The link was right but is too old: the customer can ask for a new one
            is PasswordResetLinkExpiredException -> ResponseEntity
                .status(410)
                .body(mapOf("error" to (ex.message ?: "Expired link to choose a new password")))
            // The link was right but is too old: the customer can ask for a new one
            is VerificationLinkExpiredException -> ResponseEntity
                .status(410)
                .body(mapOf("error" to (ex.message ?: "Expired verification link")))
            // The order changed status in the meantime, or is already delivered: the front reloads the orders
            is OrderStatusChangedException, is OrderStatusCannotAdvanceException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "The status of the order cannot advance")))
            // Not 422, which tells the front that the cart is empty
            is OutOfStockException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Not enough units in stock")))
            is NegativeStockException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Negative stock")))
            is ImageTooLargeException -> ResponseEntity
                .status(413)
                .body(mapOf("error" to (ex.message ?: "Image too large")))
            is UnreadableImageException -> ResponseEntity
                .status(415)
                .body(mapOf("error" to (ex.message ?: "Unreadable image")))
            is ProductImageNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "No image")))
            is OrderAlreadyPaidException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Order already paid")))
            is AccountAlreadyVerifiedException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Email already verified")))
            else -> ResponseEntity
                .status(422)
                .body(mapOf("error" to (ex.message ?: "Nominal exception")))
        }
    }

    // An uploaded file larger than spring.servlet.multipart.max-file-size, refused before reaching the domain
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleTooLargeUpload(ex: MaxUploadSizeExceededException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(413).body(mapOf("error" to "An image is 2 MB at most."))
}
