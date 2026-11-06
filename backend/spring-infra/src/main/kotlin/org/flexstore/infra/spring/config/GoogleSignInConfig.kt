package org.flexstore.infra.spring.config

import org.flexstore.domain.user.repository.GoogleIdentityVerifier
import org.flexstore.infra.spring.adapter.google.GoogleIdentityVerifierAdapter
import org.flexstore.infra.spring.adapter.google.googleIdTokenValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

private const val GOOGLE_PUBLIC_KEYS = "https://www.googleapis.com/oauth2/v3/certs"

// Signing in with Google: FLEXSTORE_GOOGLE_CLIENT_ID (the OAuth client id of the shop, created in the Google
// Cloud console) turns it on
@Configuration
open class GoogleSignInConfig(@Value("\${flexstore.security.google-client-id:}") private val clientId: String) {

    // Not the JwtDecoder bean of the shop's own tokens: that one is found by its type, so this one is not a bean
    @Bean
    open fun googleIdentityVerifier(): GoogleIdentityVerifier {
        // The keys of Google are only fetched when the first token is verified
        val googleJwtDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_PUBLIC_KEYS)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build()
        googleJwtDecoder.setJwtValidator(googleIdTokenValidator(clientId))
        return GoogleIdentityVerifierAdapter(googleJwtDecoder, clientId)
    }
}
