package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.user.*
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
            is UserCreationForbiddenException, is UserUpdateForbiddenException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Forbidden")))
            is InvalidCurrentPasswordException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Wrong current password")))
            is ProductNotFoundException, is OrderNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Not found")))
            else -> ResponseEntity
                .status(422)
                .body(mapOf("error" to (ex.message ?: "Nominal exception")))
        }
    }
}