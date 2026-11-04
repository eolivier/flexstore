package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.domain.email.VerificationToken
import org.flexstore.domain.user.model.RefreshToken
import java.security.MessageDigest
import java.util.HexFormat

// The token is the secret of a link: the database only keeps its hash, and the token is found back by hashing it
fun VerificationToken.hash(): String = sha256Of(value)

// The refresh token is the secret of a session: only its hash is kept, as for the links
fun RefreshToken.hash(): String = sha256Of(value)

private fun sha256Of(secret: String): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(secret.toByteArray()))
