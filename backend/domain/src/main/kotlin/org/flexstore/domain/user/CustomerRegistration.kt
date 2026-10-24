package org.flexstore.domain.user

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

// A customer creates their own account. The id is chosen by the caller, which can then read the created account
data class CustomerRegistration(
    val userId: UserId,
    val fullName: FullName,
    val email: Email,
    val password: PlainPassword,
    val phoneNumber: PhoneNumber,
    val postalAddress: PostalAddress
) {
    // A registered customer is a simple user whose email is not verified yet
    fun customer() = User.DefinedUser(
        id = userId,
        fullName = fullName,
        email = email,
        password = password,
        role = Role.USER,
        status = AccountStatus.PENDING_VERIFICATION,
        phoneNumber = phoneNumber,
        postalAddress = postalAddress
    )

    fun isComplete() =
        fullName.isComplete() && email.value.isNotBlank() && phoneNumber.isGiven() && postalAddress.isComplete()

    fun isIncomplete() = !isComplete()
}

data class IncompleteRegistrationException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
