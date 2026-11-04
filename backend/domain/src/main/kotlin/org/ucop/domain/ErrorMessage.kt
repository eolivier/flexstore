package org.ucop.domain

// The message of a domain exception: never empty, so that every failure says what happened
data class ErrorMessage(val value: String) {
    init {
        require(value.isNotEmpty()) { "An error message must not be empty" }
    }
}
