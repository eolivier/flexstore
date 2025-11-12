package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.entity.UserAlreadyExists
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.ucop.domain.NominalException

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(NominalException::class)
    fun handleNominalException(ex: NominalException): ResponseEntity<String> {
        if (ex is UserAlreadyExists) {
            return ResponseEntity
                .status(409)
                .body(ex.message ?: "User already exits")
        }
        return ResponseEntity
            .status(422)
            .body(ex.message ?: "Nominal exception")
    }
}