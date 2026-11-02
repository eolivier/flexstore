package org.flexstore.domain.user

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.infra.repository.InMemoryUserRepository

private fun userOf(id: String, role: Role) = DefinedUser(
    ValidUserId(id), FullName(FirstName("Jane"), LastName("Doe")), Email("$id@flexstore.fr"),
    HashedPassword("hashed"), role
)

// The administrator who runs the administration use cases in the tests
val testAdministrator = userOf("admin-1", Role.ADMIN)

// A customer, refused by the administration use cases
val testCustomer = userOf("customer-1", Role.USER)

// A user that does not exist, refused by the administration use cases
val unknownUser = ValidUserId("unknown")

// The users of the tests of the administration use cases: the administrator and a customer
fun usersOfTheAdministration() = InMemoryUserRepository().apply {
    save(testAdministrator)
    save(testCustomer)
}
