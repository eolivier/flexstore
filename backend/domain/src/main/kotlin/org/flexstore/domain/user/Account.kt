package org.flexstore.domain.user

// The account of a user, as they and the administrators see it: everything but the password
data class Account(
    val id: UserId,
    val fullName: FullName,
    val email: Email,
    val role: Role,
    val status: AccountStatus,
    val phoneNumber: PhoneNumber?,
    val postalAddresses: PostalAddresses
)
