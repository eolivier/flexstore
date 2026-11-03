package org.flexstore.domain.user

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.infra.repository.InMemoryUserRepository

// A customer of the tests of the customer use cases, which check that the user exists
fun customerOf(id: String) = DefinedUser(
    ValidUserId(id), FullName(FirstName("Jane"), LastName("Doe")), Email("$id@flexstore.fr"),
    HashedPassword("hashed"), Role.USER
)

// The users of those tests: one customer for each id given
fun customersWithIds(vararg ids: String) = InMemoryUserRepository().apply {
    ids.forEach { id -> save(customerOf(id)) }
}

// A user that does not exist, refused by the use cases that check that the user exists
val unknownCustomerId = ValidUserId("unknown-customer")

// A customer whose account has the given status: refused when it is suspended, locked or disabled
val blockedCustomerId = ValidUserId("blocked-customer")

fun customerWithStatus(status: AccountStatus) = DefinedUser(
    blockedCustomerId, FullName(FirstName("Jane"), LastName("Doe")), Email("blocked@flexstore.fr"),
    HashedPassword("hashed"), Role.USER, status
)
