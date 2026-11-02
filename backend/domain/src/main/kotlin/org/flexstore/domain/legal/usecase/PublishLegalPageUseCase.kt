package org.flexstore.domain.legal.usecase

import org.flexstore.domain.legal.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator publishes a new version of a legal page: its text is written, 50 000 characters at most. It
// becomes the current one of its kind
class PublishLegalPageUseCase(private val legalPageRepository: LegalPageRepository) : UseCase<LegalPage> {

    override fun getPreConditions(): PreConditions<LegalPage> = PreConditions(textIsValidCondition())

    override fun getNominalScenario(): NominalScenario<LegalPage> =
        NominalScenario(Step { legalPage -> legalPageRepository.save(legalPage) })

    override fun getPostConditions(): PostConditions<LegalPage> = PostConditions(legalPageIsCurrentCondition())

    private fun textIsValidCondition() = PreCondition<LegalPage> { legalPage ->
        val reason = when {
            legalPage.text.isBlank() ->"its text is not empty"
            legalPage.text.isTooLong() -> "its text is 50 000 characters at most"
            else -> null
        }
        if (reason != null) throw InvalidLegalPageException(ErrorMessage("The legal page is not valid: $reason."))
    }

    private fun legalPageIsCurrentCondition() = PostCondition<LegalPage> { legalPage ->
        if (legalPageRepository.current(legalPage.kind).id != legalPage.id) {
            throw LegalPagePublicationFailed(ErrorMessage("The legal page was not published."))
        }
    }
}
