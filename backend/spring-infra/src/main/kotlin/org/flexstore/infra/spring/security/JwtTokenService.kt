package org.flexstore.infra.spring.security

import org.flexstore.domain.user.model.Account
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
    @Value("\${flexstore.security.jwt-ttl:15m}") private val ttl: Duration
) {

    fun issue(account: Account): IssuedToken {
        val now = Instant.now()
        val expiresAt = now.plus(ttl)
        val claims = JwtClaimsSet.builder()
            .issuer("flexstore")
            .subject(account.id.value)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("email", account.email.value)
            .claim(ROLES_CLAIM, listOf(account.role.name))
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return IssuedToken(jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue, expiresAt)
    }
}
