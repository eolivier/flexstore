package org.flexstore.infra.spring.adapter.google

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.time.Instant
import javax.crypto.spec.SecretKeySpec

private const val SHOP_CLIENT_ID = "shop-client-id.apps.googleusercontent.com"

// The ID tokens of Google are signed with RS256 keys that Google publishes; here they are signed with a local
// key, so that the checks that the adapter adds (issuer, audience, verified email) are tested offline
class GoogleIdentityVerifierAdapterTest {

    private val secretKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "HmacSHA256")
    private val encoder = NimbusJwtEncoder(ImmutableSecret(secretKey))
    private val decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build()
        .also { it.setJwtValidator(googleIdTokenValidator(SHOP_CLIENT_ID)) }
    private val verifier = GoogleIdentityVerifierAdapter(decoder, SHOP_CLIENT_ID)

    private fun tokenOf(configure: JwtClaimsSet.Builder.() -> Unit = {}): GoogleIdToken {
        val claims = JwtClaimsSet.builder()
            .issuer("https://accounts.google.com")
            .audience(listOf(SHOP_CLIENT_ID))
            .subject("google-subject")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .claim("email", "Jane.Doe@Example.com")
            .claim("email_verified", true)
            .claim("given_name", "Jane")
            .claim("family_name", "Doe")
            .apply(configure)
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return GoogleIdToken(encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue)
    }

    @Test
    fun `a token of Google for this shop should give the name and the email of the person`() {
        // Arrange
        val idToken = tokenOf()
        // Act
        val googleIdentity = verifier.verify(idToken)
        // Assert
        assertThat(googleIdentity.fullName).isEqualTo(FullName(FirstName("Jane"), LastName("Doe")))
        assertThat(googleIdentity.email).isEqualTo(Email("jane.doe@example.com"))
    }

    @Test
    fun `a token whose issuer is written without the scheme should be accepted too`() {
        // Arrange
        val idToken = tokenOf { issuer("accounts.google.com") }
        // Act
        val googleIdentity = verifier.verify(idToken)
        // Assert
        assertThat(googleIdentity.email).isEqualTo(Email("jane.doe@example.com"))
    }

    @Test
    fun `a person without family name should be given by the name that Google knows`() {
        // Arrange
        val idToken = tokenOf {
            claims { it.remove("given_name"); it.remove("family_name") }
            claim("name", "Madonna")
        }
        // Act
        val googleIdentity = verifier.verify(idToken)
        // Assert
        assertThat(googleIdentity.fullName).isEqualTo(FullName(FirstName("Madonna"), LastName("")))
    }

    @Test
    fun `a token meant for another application should be refused`() {
        // Arrange
        val idToken = tokenOf { audience(listOf("another-application")) }
        // Act
        val thrown = catchThrowable { verifier.verify(idToken) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidGoogleIdTokenException::class.java)
    }

    @Test
    fun `a token not issued by Google should be refused`() {
        // Arrange
        val idToken = tokenOf { issuer("https://evil.example.com") }
        // Act
        val thrown = catchThrowable { verifier.verify(idToken) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidGoogleIdTokenException::class.java)
    }

    @Test
    fun `an expired token should be refused`() {
        // Arrange
        val idToken = tokenOf {
            issuedAt(Instant.now().minusSeconds(7200))
            expiresAt(Instant.now().minusSeconds(3600))
        }
        // Act
        val thrown = catchThrowable { verifier.verify(idToken) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidGoogleIdTokenException::class.java)
    }

    @Test
    fun `an email that Google did not verify should be refused`() {
        // Arrange
        val idToken = tokenOf { claim("email_verified", false) }
        // Act
        val thrown = catchThrowable { verifier.verify(idToken) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidGoogleIdTokenException::class.java)
    }

    @Test
    fun `a token that is not a token should be refused`() {
        // Act
        val thrown = catchThrowable { verifier.verify(GoogleIdToken("not-a-jwt")) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidGoogleIdTokenException::class.java)
    }

    @Test
    fun `every token should be refused while the client id of the shop is missing`() {
        // Arrange
        val verifierWithoutClientId = GoogleIdentityVerifierAdapter(decoder, "")
        // Act
        val thrown = catchThrowable { verifierWithoutClientId.verify(tokenOf()) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidGoogleIdTokenException::class.java)
            .hasMessage("Signing in with Google is not set up.")
    }
}
