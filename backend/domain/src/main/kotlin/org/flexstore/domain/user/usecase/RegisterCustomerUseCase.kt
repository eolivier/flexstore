package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// A customer creates their own account, which waits for the verification of their email
class RegisterCustomerUseCase(private val userRepository: UserRepository) : UseCase<CustomerRegistration> {

    override fun getPreConditions(): PreConditions<CustomerRegistration> = PreConditions(
        registrationIsCompleteCondition(),
        passwordIsLongEnoughCondition(),
        emailIsNotUsedCondition()
    )

    override fun getNominalScenario(): NominalScenario<CustomerRegistration> =
        // The repository hashes the password
        NominalScenario(Step { registration -> userRepository.save(registration.customer()) })

    override fun getPostConditions(): PostConditions<CustomerRegistration> =
        PostConditions(accountExistsCondition())

    private fun registrationIsCompleteCondition() = PreCondition<CustomerRegistration> { registration ->
        if (registration.isIncomplete()) {
            throw IncompleteRegistrationException(
                ErrorMessage("First name, last name, email, phone number and postal address are required.")
            )
        }
    }

    private fun passwordIsLongEnoughCondition() = PreCondition<CustomerRegistration> { registration ->
        if (registration.password.value.length < PASSWORD_MIN_LENGTH) {
            val message = ErrorMessage("Password must be at least $PASSWORD_MIN_LENGTH characters.")
            throw InvalidPasswordException(message)
        }
    }

    private fun emailIsNotUsedCondition() = PreCondition<CustomerRegistration> { registration ->
        if (userRepository.emailIsAlreadyUsed(registration.email)) {
            throw UserAlreadyExists(ErrorMessage("User with email ${registration.email.value} already exists."))
        }
    }

    private fun accountExistsCondition() = PostCondition<CustomerRegistration> { registration ->
        if (userRepository.notExists(registration.userId)) {
            throw UserCreationFailed(ErrorMessage("The account of ${registration.email.value} was not created."))
        }
    }
}
