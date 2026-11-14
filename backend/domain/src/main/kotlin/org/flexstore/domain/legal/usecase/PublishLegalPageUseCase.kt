package org.flexstore.domain.legal.usecase

import org.flexstore.domain.legal.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator publishes a new version of a legal page: its text is written, 50 000 characters at most. It
// becomes the current one of its kind
class PublishLegalPageUseCase(
    private val legalPageRepository: LegalPageRepository
) : UseCase<LegalPagePublication> {

    override fun getPreConditions(): PreConditions<LegalPagePublication> =
        PreConditions(publisherIsAdministratorCondition(), textIsValidCondition())

    override fun getNominalScenario(): NominalScenario<LegalPagePublication> =
        NominalScenario(Step { publication -> legalPageRepository.save(publication.legalPage) })

    override fun getPostConditions(): PostConditions<LegalPagePublication> =
        PostConditions(legalPageIsCurrentCondition())

    // The publisher, resolved before the use case runs, must be an Admin
    private fun publisherIsAdministratorCondition() = PreCondition<LegalPagePublication> { publication ->
        if (publication.publisher.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can publish a legal page.")
            throw LegalPagePublicationForbiddenException(message)
        }
    }

    private fun textIsValidCondition() = PreCondition<LegalPagePublication> { publication ->
        val text = publication.legalPage.text
        if (text.isBlank()) throw invalidLegalPage("its text is not empty")
        if (text.isTooLong()) throw invalidLegalPage("its text is 50 000 characters at most")
    }

    private fun invalidLegalPage(reason: String) =
        InvalidLegalPageException(ErrorMessage("The legal page is not valid: $reason."))

    private fun legalPageIsCurrentCondition() = PostCondition<LegalPagePublication> { publication ->
        val legalPage = publication.legalPage
        if (legalPageRepository.current(legalPage.kind).id != legalPage.id) {
            throw LegalPagePublicationFailed(ErrorMessage("The legal page was not published."))
        }
    }
}
