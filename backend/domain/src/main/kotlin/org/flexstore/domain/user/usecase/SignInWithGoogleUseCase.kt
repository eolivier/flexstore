package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*
import java.util.UUID

// A person signs in with their Google account, which the sign-in service verified before. The account that has
// their email is the one they sign in to; a customer who has none gets one
class SignInWithGoogleUseCase(private val userRepository: UserRepository) : UseCase<GoogleIdentity> {

    override fun getPreConditions(): PreConditions<GoogleIdentity> = PreConditions(accountAllowsSignInCondition())

    override fun getNominalScenario(): NominalScenario<GoogleIdentity> =
        NominalScenario(Step { googleIdentity -> createAccountOfNewCustomer(googleIdentity) })

    override fun getPostConditions(): PostConditions<GoogleIdentity> =
        PostConditions(accountExistsCondition())

    private fun createAccountOfNewCustomer(googleIdentity: GoogleIdentity) {
        if (userRepository.notExistsBasedOn(googleIdentity.email)) {
            // The repository hashes the password
            userRepository.save(googleIdentity.customer(UserId.nextId(), unknownPassword()))
        }
    }

    private fun unknownPassword() = PlainPassword(UUID.randomUUID().toString() + UUID.randomUUID())

    private fun accountAllowsSignInCondition() = PreCondition<GoogleIdentity> { googleIdentity ->
        val user = userRepository.findByEmail(googleIdentity.email)
        if (user is User.DefinedUser && user.status.refusesSignIn()) {
            val message = ErrorMessage("The account of ${googleIdentity.email.value} is ${user.status}.")
            throw AccountSignInRefusedException(user.status, message)
        }
    }

    private fun accountExistsCondition() = PostCondition<GoogleIdentity> { googleIdentity ->
        if (userRepository.notExistsBasedOn(googleIdentity.email)) {
            throw UserGoogleAuthenticationException(ErrorMessage("User authentication failed."))
        }
    }
}
