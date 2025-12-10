package org.flexstore.domain.port

/**
 * Port for password encoding operations.
 * This interface abstracts the password hashing implementation from the domain layer.
 */
interface PasswordEncoder {
    /**
     * Encodes a raw password into a secure hash.
     * @param rawPassword the plain text password
     * @return the encoded password hash
     */
    fun encode(rawPassword: String): String

    /**
     * Verifies that a raw password matches an encoded password hash.
     * @param rawPassword the plain text password to verify
     * @param encodedPassword the encoded password hash to compare against
     * @return true if the passwords match, false otherwise
     */
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
