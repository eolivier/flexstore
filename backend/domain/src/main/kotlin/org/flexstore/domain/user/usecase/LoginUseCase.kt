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
        println("#[BEGIN] LoginUseCase.getPreConditions")
        val preConditions = PreConditions(userExistsByEmailCondition(), passwordMatchesCondition())
        println("#[END] LoginUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<LoginRequest> {
        println("#[BEGIN] LoginUseCase.getNominalScenario")
        // Steps
        val authenticateUserStep = Step<LoginRequest> { request -> 
            authenticatedUser = userRepository.findByEmail(request.email)
        }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(authenticateUserStep))
        println("#[END] LoginUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<LoginRequest>> {
        println("#[BEGIN] LoginUseCase.getPostConditions")
        val postConditions = listOf(userAuthenticatedCondition())
        println("#[END] LoginUseCase.getPostConditions")
        return postConditions
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

    private fun userAuthenticatedCondition() = PostCondition<LoginRequest> { request ->
        if (authenticatedUser is User.UndefinedUser) {
            throw InvalidCredentialsException(ErrorMessage("User authentication failed."))
        }
    }
}
