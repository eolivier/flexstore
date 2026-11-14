package org.flexstore.domain.user.model

import org.ucop.domain.ErrorMessage
import org.ucop.domain.PreConditionException

// A customer creates their own account. The id is chosen by the caller, which can then read the created account.
// The express registration only gives the names, the email and the password: the phone number and the postal
// address are given later, from the account
data class CustomerRegistration(
    val userId: UserId,
    val fullName: FullName,
    val email: Email,
    val password: PlainPassword,
    val phoneNumber: PhoneNumber,
    val postalAddresses: PostalAddresses,
    val customerType: CustomerType
) {
    // A customer who buys for themselves and gives their phone number and their postal address
    constructor(
        userId: UserId,
        fullName: FullName,
        email: Email,
        password: PlainPassword,
        phoneNumber: PhoneNumber,
        postalAddress: PostalAddress
    ) : this(
        userId, fullName, email, password, phoneNumber, PostalAddresses(postalAddress), CustomerType.Individual
    )

    // The express registration of a customer who buys for themselves
    constructor(userId: UserId, fullName: FullName, email: Email, password: PlainPassword) :
        this(userId, fullName, email, password, PhoneNumber.none(), PostalAddresses.none(), CustomerType.Individual)

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
        postalAddresses = addressesForTheCustomer(),
        customerType = customerType
    )

    private fun addressesForTheCustomer() = PostalAddresses(
        postalAddresses.asList().map { PostalAddress(customerType.recipientFor(fullName), it.location) }
    )

    fun isComplete() = fullName.isComplete() && email.value.isNotBlank() && postalAddresses.areComplete()

    fun isIncomplete() = !isComplete()
}

data class IncompleteRegistrationException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
