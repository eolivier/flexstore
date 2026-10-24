package org.flexstore.infra.spring.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt

// Requests authenticated with a JWT, as issued by the login
fun asAdmin(userId: String = "admin") =
    jwt().jwt { it.subject(userId) }.authorities(SimpleGrantedAuthority("ROLE_ADMIN"))

fun asUser(userId: String) =
    jwt().jwt { it.subject(userId) }.authorities(SimpleGrantedAuthority("ROLE_USER"))
