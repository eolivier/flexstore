package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

data class LoginRequest(val email: Email, val password: Password)

class LoginUseCase(
    private val userRepository: UserRepository
) : UseCase<LoginRequest> {

    lateinit var authenticatedUser: User

    override fun getPreConditions(): PreConditions<LoginRequest> {
        // The status is only checked once the password is right: it does not reveal the accounts to a wrong password
        return PreConditions(userExistsByEmailCondition(), passwordMatchesCondition(), accountAllowsSignInCondition())
    }

    override fun getNominalScenario(): NominalScenario<LoginRequest> {
        // Steps
        val authenticateUserStep = Step<LoginRequest> { request -> 
            authenticatedUser = userRepository.findByEmail(request.email)
        }
        // Nominal scenario
        return NominalScenario(listOf(authenticateUserStep))
    }

    override fun getPostConditions(): PostConditions<LoginRequest> {
        return PostConditions(userAuthenticatedCondition())
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<LoginRequest>> {
        return emptyMap()
    }

    private fun userExistsByEmailCondition() = PreCondition<LoginRequest> { request ->
        val user = userRepository.findByEmail(request.email)
        if (user is User.UndefinedUser) {
            throw UserNotFoundByEmailException(ErrorMessage("User with email ${request.email.value} does not exist."))
        }
    }

    private fun passwordMatchesCondition() = PreCondition<LoginRequest> { request ->
        if (userRepository.passwordDoesNotMatch(request.email, request.password)) {
            throw InvalidCredentialsException(ErrorMessage("Invalid credentials."))
        }
    }

    private fun accountAllowsSignInCondition() = PreCondition<LoginRequest> { request ->
        val user = userRepository.findByEmail(request.email) as User.DefinedUser
        if (user.status.refusesSignIn()) {
            throw AccountSignInRefusedException(user.status, ErrorMessage("The account of ${request.email.value} is ${user.status}."))
        }
    }

    private fun userAuthenticatedCondition() = PostCondition<LoginRequest> { request ->
        if (authenticatedUser is User.UndefinedUser) {
            throw InvalidCredentialsException(ErrorMessage("User authentication failed."))
        }
    }
}
