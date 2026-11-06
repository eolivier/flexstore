package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*
import java.util.UUID

// A person signs in with their Google account. The account that has their email is the one they sign in to; a
// customer who has none gets one. The use case keeps the user it authenticated: one per sign-in
class SignInWithGoogleUseCase(
    private val userRepository: UserRepository,
    private val googleIdentityVerifier: GoogleIdentityVerifier
) : UseCase<GoogleIdToken> {

    lateinit var authenticatedUser: User
    private lateinit var googleIdentity: GoogleIdentity

    override fun getPreConditions(): PreConditions<GoogleIdToken> = PreConditions(
        googleIdentityIsVerifiedCondition(),
        accountAllowsSignInCondition()
    )

    override fun getNominalScenario(): NominalScenario<GoogleIdToken> = NominalScenario(
        Step { createAccountOfNewCustomer() },
        Step { authenticatedUser = userRepository.findByEmail(googleIdentity.email) }
    )

    override fun getPostConditions(): PostConditions<GoogleIdToken> =
        PostConditions(userAuthenticatedCondition())

    private fun createAccountOfNewCustomer() {
        if (userRepository.notExistsBasedOn(googleIdentity.email)) {
            // The repository hashes the password
            userRepository.save(googleIdentity.customer(UserId.nextId(), unknownPassword()))
        }
    }

    private fun unknownPassword() = PlainPassword(UUID.randomUUID().toString() + UUID.randomUUID())

    private fun googleIdentityIsVerifiedCondition() = PreCondition<GoogleIdToken> { idToken ->
        googleIdentity = googleIdentityVerifier.verify(idToken)
    }

    private fun accountAllowsSignInCondition() = PreCondition<GoogleIdToken> {
        val user = userRepository.findByEmail(googleIdentity.email)
        if (user is User.DefinedUser && user.status.refusesSignIn()) {
            val message = ErrorMessage("The account of ${googleIdentity.email.value} is ${user.status}.")
            throw AccountSignInRefusedException(user.status, message)
        }
    }

    private fun userAuthenticatedCondition() = PostCondition<GoogleIdToken> {
        if (authenticatedUser is User.UndefinedUser) {
            throw InvalidCredentialsException(ErrorMessage("User authentication failed."))
        }
    }
}
