package org.flexstore.domain.user.model

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

// A customer creates their own account. The id is chosen by the caller, which can then read the created account
data class CustomerRegistration(
    val userId: UserId,
    val fullName: FullName,
    val email: Email,
    val password: PlainPassword,
    val phoneNumber: PhoneNumber,
    val postalAddress: PostalAddress,
    val customerType: CustomerType
) {
    // A customer who buys for themselves
    constructor(
        userId: UserId,
        fullName: FullName,
        email: Email,
        password: PlainPassword,
        phoneNumber: PhoneNumber,
        postalAddress: PostalAddress
    ) : this(userId, fullName, email, password, phoneNumber, postalAddress, CustomerType.Individual)

    // A registered customer is a simple user whose email is not verified yet. The address they give is sent to
    // them, or to their company for their attention
    fun customer() = User.DefinedUser(
        id = userId,
        fullName = fullName,
        email = email,
        password = password,
        role = Role.USER,
        status = AccountStatus.PENDING_VERIFICATION,
        phoneNumber = phoneNumber,
        postalAddresses = PostalAddresses(firstAddress()),
        customerType = customerType
    )

    private fun firstAddress() = PostalAddress(customerType.recipientFor(fullName), postalAddress.location)

    fun isComplete() =
        fullName.isComplete() && email.value.isNotBlank() && phoneNumber.isGiven() && postalAddress.isComplete()

    fun isIncomplete() = !isComplete()
}

data class IncompleteRegistrationException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
