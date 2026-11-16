package org.flexstore.domain.legal.usecase

import org.flexstore.domain.actor.verifyCanRead
import org.flexstore.domain.legal.*
import org.ucop.domain.entity.*

// The current version of a legal page, as read by everyone
// A reader asks for the current version of a kind of legal page
data class LegalPageReading(val reader: Actor, val kind: LegalPageKind)

class ReadLegalPageUseCase(private val legalPageRepository: LegalPageRepository) :
    QueryUseCase<LegalPageReading, LegalPage> {

    override fun getPreConditions(): PreConditions<LegalPageReading> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() =
        NominalQuery<LegalPageReading, LegalPage> { reading ->
            legalPageRepository.current(reading.kind)
        }

    override fun getPostConditions(): PostConditions<LegalPage> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<LegalPageReading> { reading ->
        reading.reader.verifyCanRead("the legal pages", ::LegalPageReadingForbiddenException)
    }
}
