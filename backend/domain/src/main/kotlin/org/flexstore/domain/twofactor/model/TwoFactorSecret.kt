package org.flexstore.domain.twofactor.model

import org.flexstore.domain.user.model.Email
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val SECRET_BYTE_LENGTH = 20 // 160 bits, the length recommended by RFC 4226 for HMAC-SHA1
private const val TIME_STEP_SECONDS = 30L // RFC 6238: a new code every 30 seconds
private const val ADJACENT_TIME_STEPS = 1L // Tolerates the step just before and just after, for clock drift
private const val CODE_MODULUS = 1_000_000 // 6 digits
private const val HMAC_ALGORITHM = "HmacSHA1"
private const val COUNTER_BYTES = 8
private const val LOW_NIBBLE_MASK = 0xf
private const val SIGN_BIT_MASK = 0x7f
private const val BYTE_MASK = 0xff
private const val THIRD_BYTE_SHIFT = 24
private const val SECOND_BYTE_SHIFT = 16
private const val FIRST_BYTE_SHIFT = 8

// The shared secret of an authenticator app: a RFC 6238 TOTP (HMAC-SHA1, 30-second step, 6 digits), Base32-coded
data class TwoFactorSecret(val value: String) {

    // The code an authenticator app shows for this secret at the given moment
    fun codeAt(instant: Instant): TwoFactorCode = codeAtTimeStep(timeStepAt(instant))

    // Tolerates the time step just before and just after the given moment, for the clock drift of the device
    fun matches(code: TwoFactorCode, instant: Instant): Boolean {
        val timeStep = timeStepAt(instant)
        val toleratedSteps = -ADJACENT_TIME_STEPS..ADJACENT_TIME_STEPS
        return toleratedSteps.any { offset -> codeAtTimeStep(timeStep + offset) == code }
    }

    fun doesNotMatch(code: TwoFactorCode, instant: Instant) = !matches(code, instant)

    fun provisioningUri(accountName: Email, issuer: TwoFactorIssuer): ProvisioningUri =
        ProvisioningUri.of(this, accountName, issuer)

    private fun timeStepAt(instant: Instant) = instant.epochSecond / TIME_STEP_SECONDS

    private fun codeAtTimeStep(timeStep: Long): TwoFactorCode {
        val hash = hmacSha1(Base32.decode(value), counterBytesOf(timeStep))
        val offset = hash.last().toInt() and LOW_NIBBLE_MASK
        val binaryCode = ((hash[offset].toInt() and SIGN_BIT_MASK) shl THIRD_BYTE_SHIFT) or
            ((hash[offset + 1].toInt() and BYTE_MASK) shl SECOND_BYTE_SHIFT) or
            ((hash[offset + 2].toInt() and BYTE_MASK) shl FIRST_BYTE_SHIFT) or
            (hash[offset + 3].toInt() and BYTE_MASK)
        return TwoFactorCode.of(binaryCode % CODE_MODULUS)
    }

    private fun counterBytesOf(timeStep: Long): ByteArray {
        val counter = ByteArray(COUNTER_BYTES)
        var remainder = timeStep
        for (index in COUNTER_BYTES - 1 downTo 0) {
            counter[index] = (remainder and BYTE_MASK.toLong()).toByte()
            remainder = remainder shr Byte.SIZE_BITS
        }
        return counter
    }

    private fun hmacSha1(key: ByteArray, message: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        return mac.doFinal(message)
    }

    companion object {
        // A new random secret, generated for an enrollment
        fun next(): TwoFactorSecret {
            val randomBytes = ByteArray(SECRET_BYTE_LENGTH)
            SecureRandom().nextBytes(randomBytes)
            return TwoFactorSecret(Base32.encode(randomBytes))
        }
    }
}

// The name shown by the authenticator app next to the account
data class TwoFactorIssuer(val value: String) {
    companion object {
        fun flexstore() = TwoFactorIssuer("Flexstore")
    }
}

// The otpauth:// link an authenticator app reads (as a QR code, or typed manually) to add the account
data class ProvisioningUri(val value: String) {
    companion object {
        fun of(secret: TwoFactorSecret, accountName: Email, issuer: TwoFactorIssuer): ProvisioningUri {
            val label = urlEncoded("${issuer.value}:${accountName.value}")
            val issuerParameter = urlEncoded(issuer.value)
            val query = "secret=${secret.value}&issuer=$issuerParameter&algorithm=SHA1&digits=6&period=30"
            return ProvisioningUri("otpauth://totp/$label?$query")
        }

        private fun urlEncoded(text: String) = URLEncoder.encode(text, StandardCharsets.UTF_8)
    }
}
