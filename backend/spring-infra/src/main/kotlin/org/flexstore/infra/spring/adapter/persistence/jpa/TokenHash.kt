package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.domain.email.VerificationToken
import java.security.MessageDigest
import java.util.HexFormat

// The token is the secret of a link: the database only keeps its hash, and the token is found back by hashing it
fun VerificationToken.hash(): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.toByteArray()))
