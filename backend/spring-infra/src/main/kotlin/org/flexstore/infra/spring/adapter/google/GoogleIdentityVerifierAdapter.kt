package org.flexstore.infra.spring.adapter.google

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.GoogleIdentityVerifier
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.ucop.domain.ErrorMessage

private val GOOGLE_ISSUERS = listOf("https://accounts.google.com", "accounts.google.com")

// The token of Google must be valid (signature, not expired), issued by Google, and meant for this shop
fun googleIdTokenValidator(clientId: String): OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(
    JwtValidators.createDefault(),
    OAuth2TokenValidator { jwt ->
        checking(jwt.getClaimAsString("iss") in GOOGLE_ISSUERS, "Not issued by Google.")
    },
    OAuth2TokenValidator { jwt ->
        checking(jwt.audience.orEmpty().contains(clientId), "Not meant for this shop.")
    }
)

private fun checking(isRight: Boolean, description: String) =
    if (isRight) OAuth2TokenValidatorResult.success()
    else OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_token", description, null))

// Adapter of the port to Google: reads the ID token that the "Sign in with Google" button gave to the browser.
// The decoder checks the signature with the public keys of Google; signing in with Google is not set up (and
// every token is refused) while the client id of the shop is missing
class GoogleIdentityVerifierAdapter(
    private val jwtDecoder: JwtDecoder,
    private val clientId: String
) : GoogleIdentityVerifier {

    override fun verify(idToken: GoogleIdToken): GoogleIdentity {
        if (clientId.isBlank()) {
            throw InvalidGoogleIdTokenException(ErrorMessage("Signing in with Google is not set up."))
        }
        val jwt = decode(idToken)
        // Google vouches for the email only when it is verified
        if (jwt.getClaimAsBoolean("email_verified") != true || jwt.getClaimAsString("email").isNullOrBlank()) {
            throw InvalidGoogleIdTokenException(ErrorMessage("The Google account has no verified email."))
        }
        return GoogleIdentity(fullNameOf(jwt), Email(jwt.getClaimAsString("email").trim().lowercase()))
    }

    private fun decode(idToken: GoogleIdToken): Jwt = try {
        jwtDecoder.decode(idToken.value)
    } catch (invalid: JwtException) {
        throw InvalidGoogleIdTokenException(ErrorMessage("The Google token is refused: ${invalid.message}"))
    }

    // A person who gave a single name has only the name that Google knows
    private fun givenNameOf(jwt: Jwt) =
        jwt.getClaimAsString("given_name").orEmpty().ifBlank { jwt.getClaimAsString("name").orEmpty() }

    private fun fullNameOf(jwt: Jwt) = FullName(
        FirstName(givenNameOf(jwt)),
        LastName(jwt.getClaimAsString("family_name").orEmpty())
    )
}
