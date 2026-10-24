package org.flexstore.infra.spring.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt

// The administration account created by the migrations: the domain checks that a user creator is an administrator
const val ADMIN_ACCOUNT_ID = "u-admin"

// Requests authenticated with a JWT, as issued by the login
fun asAdmin(userId: String = ADMIN_ACCOUNT_ID) =
    jwt().jwt { it.subject(userId) }.authorities(SimpleGrantedAuthority("ROLE_ADMIN"))

fun asUser(userId: String) =
    jwt().jwt { it.subject(userId) }.authorities(SimpleGrantedAuthority("ROLE_USER"))
