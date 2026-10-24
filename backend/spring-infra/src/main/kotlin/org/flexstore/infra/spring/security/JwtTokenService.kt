package org.flexstore.infra.spring.security

import org.flexstore.domain.user.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

data class IssuedToken(val value: String, val expiresAt: Instant)

@Service
class JwtTokenService(
    private val jwtEncoder: JwtEncoder,
    @Value("\${flexstore.security.jwt-ttl:1h}") private val ttl: Duration
) {

    fun issue(user: User.DefinedUser): IssuedToken {
        val now = Instant.now()
        val expiresAt = now.plus(ttl)
        val claims = JwtClaimsSet.builder()
            .issuer("flexstore")
            .subject(user.id.value)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("email", user.email.value)
            .claim(ROLES_CLAIM, listOf(user.role.name))
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return IssuedToken(jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue, expiresAt)
    }
}
