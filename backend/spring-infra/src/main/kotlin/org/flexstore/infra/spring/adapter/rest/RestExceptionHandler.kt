package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.entity.InvalidCredentialsException
import org.flexstore.domain.entity.UserAlreadyExists
import org.flexstore.domain.entity.UserNotFoundByEmailException
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
            is InvalidCredentialsException -> ResponseEntity
                .status(401)
                .body(mapOf("error" to (ex.message ?: "Invalid credentials")))
            is UserNotFoundByEmailException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "User not found")))
            else -> ResponseEntity
                .status(422)
                .body(mapOf("error" to (ex.message ?: "Nominal exception")))
        }
    }
}