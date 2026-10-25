package org.flexstore.infra.spring.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
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
@Configuration
open class SecurityConfig(@Value("\${flexstore.security.jwt-secret:}") private val jwtSecret: String) {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    private val secretKey: SecretKey by lazy {
        val secret = jwtSecret.toByteArray()
        val keyBytes = if (secret.size >= 32) {
            secret
        } else {
            // Without a configured secret, tokens are only valid until the application restarts
            logger.warn("FLEXSTORE_JWT_SECRET is missing or shorter than 32 characters: using a random secret")
            ByteArray(32).also { SecureRandom().nextBytes(it) }
        }
        SecretKeySpec(keyBytes, "HmacSHA256")
    }

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(withDefaults())
            // Tokens are sent in the Authorization header, not in cookies: no CSRF risk
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/error", "/actuator/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/stocks/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/product-images").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/cart/merge").authenticated()
                    .requestMatchers("/api/cart/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()
                    // Stripe calls it: the signature of the notification authenticates it
                    .requestMatchers(HttpMethod.POST, "/api/payments/stripe/webhook").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts/email-verification").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/{id}/password").access(isUserFromPath())
                    .requestMatchers("/api/users/**").hasRole("ADMIN")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
            }
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

