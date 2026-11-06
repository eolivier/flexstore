package org.flexstore.infra.spring.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.flexstore.domain.user.usecase.ReadAccountUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

const val ROLES_CLAIM = "roles"

/**
 * Stateless authentication with JWT bearer tokens signed with HMAC-SHA256 (issued by POST /api/users/login).
 * The catalog, the cart and the login are public (merging a visitor cart requires a signed-in user);
 * managing users and products requires the ADMIN role.
 */
// The size of the key that signs the tokens (HMAC SHA-256)
private const val JWT_KEY_BYTES = 32

@Configuration
open class SecurityConfig(@Value("\${flexstore.security.jwt-secret:}") private val jwtSecret: String) {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    private val secretKey: SecretKey by lazy {
        val secret = jwtSecret.toByteArray()
        val keyBytes = if (secret.size >= JWT_KEY_BYTES) {
            secret
        } else {
            // Without a configured secret, tokens are only valid until the application restarts
            logger.warn(
                "FLEXSTORE_JWT_SECRET is missing or shorter than $JWT_KEY_BYTES characters: using a random secret"
            )
            ByteArray(JWT_KEY_BYTES).also { SecureRandom().nextBytes(it) }
        }
        SecretKeySpec(keyBytes, "HmacSHA256")
    }

    @Bean
    open fun securityFilterChain(
        http: HttpSecurity,
        readAccountUseCase: ReadAccountUseCase
    ): SecurityFilterChain {
        http
            .cors(withDefaults())
            // Tokens are sent in the Authorization header, not in cookies: no CSRF risk
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/error", "/actuator/**").permitAll()
                    // The MCP server only gives the public products, like GET /api/products
                    .requestMatchers("/mcp").permitAll()
                    // Whether the signed-in customer may review a product, before the public reading
                    .requestMatchers(HttpMethod.GET, "/api/products/{id}/review-eligibility").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/stocks/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/product-ratings").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/product-images").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/legal-pages/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/social-networks").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/cart/merge").authenticated()
                    .requestMatchers("/api/cart/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                    // The token of Google is the credential: it is verified by the back end
                    .requestMatchers(HttpMethod.POST, "/api/users/login/google").permitAll()
                    // The refresh token in the body is the credential: the access token has expired
                    .requestMatchers(HttpMethod.POST, "/api/users/refresh", "/api/users/logout").permitAll()
                    // The password is already checked: completing the challenge needs no token yet
                    .requestMatchers(HttpMethod.POST, "/api/users/login/two-factor-authentication").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()
                    // Stripe calls it: the signature of the notification authenticates it
                    .requestMatchers(HttpMethod.POST, "/api/payments/stripe/webhook").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts/email-verification").permitAll()
                    // A customer who forgot their password is not signed in
                    .requestMatchers(HttpMethod.POST, "/api/password-reset-requests", "/api/password-resets")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/{id}/password").access(isUserFromPath())
                    // Two-factor authentication is managed by the customer or administrator it belongs to
                    // (checked again by its own precondition in the domain)
                    .requestMatchers(HttpMethod.POST, "/api/users/{id}/two-factor-authentication/enrollment")
                    .access(isUserFromPath())
                    .requestMatchers(HttpMethod.POST, "/api/users/{id}/two-factor-authentication/confirmation")
                    .access(isUserFromPath())
                    .requestMatchers(HttpMethod.DELETE, "/api/users/{id}/two-factor-authentication")
                    .access(isUserFromPath())
                    .requestMatchers("/api/users/**").hasRole("ADMIN")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }
            // Not a bean: a Filter bean would also be registered outside of the security chain
            .addFilterBefore(AccountAuthorizationFilter(readAccountUseCase), AuthorizationFilter::class.java)
        return http.build()
    }

    // A user can only change their own password: the {id} of the path must be the subject of the token
    private fun isUserFromPath() = AuthorizationManager<RequestAuthorizationContext> { authentication, context ->
        val auth = authentication.get()
        AuthorizationDecision(auth.isAuthenticated && auth.name == context.variables["id"])
    }

    @Bean
    open fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableSecret(secretKey))

    @Bean
    open fun jwtDecoder(): JwtDecoder =
        NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build()

    private fun jwtAuthenticationConverter() = JwtAuthenticationConverter().apply {
        setJwtGrantedAuthoritiesConverter(JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName(ROLES_CLAIM)
            setAuthorityPrefix("ROLE_")
        })
    }
}

