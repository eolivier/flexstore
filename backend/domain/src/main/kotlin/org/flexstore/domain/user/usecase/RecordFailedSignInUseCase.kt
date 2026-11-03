package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.FailedSignInRepository
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*

// The sign-in of an account failed because of a wrong password: it is counted, and the account is locked once
// the limit is reached, which starts the count again for when an administrator reactivates it. Counts for
// nothing: an email that is not the one of an account (so that the sign-in does not reveal which accounts
// exist), and an account that cannot sign in already (suspended, locked, disabled), whose status stays as it is.
class RecordFailedSignInUseCase(
    private val userRepository: UserRepository,
    private val failedSignInRepository: FailedSignInRepository
) : UseCase<Email> {

    override fun getPreConditions(): PreConditions<Email> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<Email> = NominalScenario(Step { email ->
        val user = userRepository.findByEmail(email)
        if (user is User.DefinedUser) {
            countTheFailureOf(user)
        }
    })

    override fun getPostConditions(): PostConditions<Email> = PostConditions(failedSignInIsCountedCondition())

    private fun countTheFailureOf(user: User.DefinedUser) {
        if (user.status.refusesSignIn()) return
        val failedSignInCount = failedSignInRepository.countOf(user.id).plusOne()
        if (failedSignInCount.hasReachedLimit()) {
            userRepository.save(user.locked())
            failedSignInRepository.reset(user.id)
        } else {
            failedSignInRepository.save(user.id, failedSignInCount)
        }
    }

    // Counted, or the account is locked (or cannot sign in): either way, it is not left with no trace
    private fun failedSignInIsCountedCondition() = PostCondition<Email> { email ->
        val user = userRepository.findByEmail(email)
        if (user is User.DefinedUser && user.status.allowsSignIn() && countOf(user).isNone()) {
            throw FailedSignInNotCounted(ErrorMessage("A failed sign-in of ${email.value} was not counted."))
        }
    }

    private fun countOf(user: User.DefinedUser) = failedSignInRepository.countOf(user.id)
}

data class FailedSignInNotCounted(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
