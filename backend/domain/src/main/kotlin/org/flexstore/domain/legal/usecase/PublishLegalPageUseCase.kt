package org.flexstore.domain.legal.usecase

import org.flexstore.domain.legal.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// An administrator publishes a new version of a legal page: its text is written, 50 000 characters at most. It
// becomes the current one of its kind
class PublishLegalPageUseCase(
    private val legalPageRepository: LegalPageRepository,
    private val userRepository: UserRepository
) : UseCase<LegalPagePublication> {

    override fun getPreConditions(): PreConditions<LegalPagePublication> =
        PreConditions(publisherIsAdministratorCondition(), textIsValidCondition())

    override fun getNominalScenario(): NominalScenario<LegalPagePublication> =
        NominalScenario(Step { publication -> legalPageRepository.save(publication.legalPage) })

    override fun getPostConditions(): PostConditions<LegalPagePublication> =
        PostConditions(legalPageIsCurrentCondition())

    // The publisher must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun publisherIsAdministratorCondition() = PreCondition<LegalPagePublication> { publication ->
        if (userRepository.isNotAdministrator(publication.publisher)) {
            throw LegalPagePublicationForbiddenException(
                ErrorMessage("Only an administrator can publish a legal page.")
            )
        }
    }

    private fun textIsValidCondition() = PreCondition<LegalPagePublication> { publication ->
        val text = publication.legalPage.text
        val reason = when {
            text.isBlank() -> "its text is not empty"
            text.isTooLong() -> "its text is 50 000 characters at most"
            else -> null
        }
        if (reason != null) throw InvalidLegalPageException(ErrorMessage("The legal page is not valid: $reason."))
    }

    private fun legalPageIsCurrentCondition() = PostCondition<LegalPagePublication> { publication ->
        val legalPage = publication.legalPage
        if (legalPageRepository.current(legalPage.kind).id != legalPage.id) {
            throw LegalPagePublicationFailed(ErrorMessage("The legal page was not published."))
        }
    }
}
