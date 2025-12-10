package org.flexstore.infra.spring.adapter.security

import org.flexstore.domain.port.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

/**
 * BCrypt implementation of the PasswordEncoder port.
 * Uses Spring Security's BCryptPasswordEncoder for secure password hashing.
 */
@Component
class BCryptPasswordEncoder : PasswordEncoder {
    
    private val encoder = BCryptPasswordEncoder()
    
    override fun encode(rawPassword: String): String {
        return encoder.encode(rawPassword)
    }
    
    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return encoder.matches(rawPassword, encodedPassword)
    }
}
