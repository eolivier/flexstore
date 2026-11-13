package org.flexstore.domain.legal.usecase

import org.flexstore.domain.actor.unknownUserActor
import org.flexstore.domain.legal.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.unknownUserActor
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryLegalPageRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant

class LegalPageUseCasesTest : WithAssertions {

    private val firstPublication = PublishedAt(Instant.parse("2026-01-01T00:00:00Z"))
    private val unwrittenLegalNotice =
        LegalPage(
            LegalPageId(Identity("legal-notice-1")), LegalPageKind.LEGAL_NOTICE, LegalText(""), firstPublication
        )
    private val writtenTermsOfSale = LegalPage(
        LegalPageId(Identity("terms-of-sale-1")), LegalPageKind.TERMS_OF_SALE,
        LegalText("Article 1 - Objet"), firstPublication
    )
    private val legalPageRepository =
        InMemoryLegalPageRepository(listOf(unwrittenLegalNotice, writtenTermsOfSale))
    private val administrator = testAdministratorActor
    private val readLegalPageUseCase = ReadLegalPageUseCase(legalPageRepository)
    private val legalPageUseCases =
        LegalPageUseCases(readLegalPageUseCase, PublishLegalPageUseCase(legalPageRepository))

    @AfterEach
    fun resetClock() {
        InstantProvider.reset()
    }

    @Test
    fun `a legal page not written yet should be read with an empty text`() {
        // Act
        val legalNotice = legalPageUseCases.legalPage(unknownUserActor, LegalPageKind.LEGAL_NOTICE)
        // Assert
        assertThat(legalNotice.isWritten()).isFalse()
    }

    @Test
    fun `each kind of legal page should be read on its own`() {
        // Act
        val termsOfSale = legalPageUseCases.legalPage(unknownUserActor, LegalPageKind.TERMS_OF_SALE)
        // Assert
        assertThat(termsOfSale).isEqualTo(writtenTermsOfSale)
    }

    @Test
    fun `the published text should become the current version of its page, published now`() {
        // Arrange
        val now = Instant.parse("2026-11-02T10:00:00Z")
        InstantProvider.fixAt(now)
        val legalText = LegalText("Flexstore SAS, 1 rue de la Démo, 75001 Paris")
        // Act
        val legalNotice = legalPageUseCases.publish(administrator, LegalPageKind.LEGAL_NOTICE, legalText)
        // Assert
        assertThat(legalNotice.text).isEqualTo(legalText)
        assertThat(legalNotice.publishedAt).isEqualTo(PublishedAt(now))
        assertThat(legalNotice.id).isNotEqualTo(unwrittenLegalNotice.id)
    }

    @Test
    fun `publishing a legal page should keep its previous version and leave the other page as it was`() {
        // Act
        val legalText = LegalText("Article 1 - Objet et champ")
        legalPageUseCases.publish(administrator, LegalPageKind.TERMS_OF_SALE, legalText)
        // Assert
        assertThat(legalPageRepository.findBy(writtenTermsOfSale.id)).isEqualTo(writtenTermsOfSale)
        val legalPage = legalPageUseCases.legalPage(unknownUserActor, LegalPageKind.LEGAL_NOTICE)
        assertThat(legalPage).isEqualTo(unwrittenLegalNotice)
    }

    @Test
    fun `a customer should not publish a legal page`() {
        // Act
        val thrown = catchThrowable {
            legalPageUseCases.publish(
                testCustomerActor, LegalPageKind.TERMS_OF_SALE, LegalText("Article 1 - Autre")
            )
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(LegalPagePublicationForbiddenException::class.java)
        val legalPage = legalPageUseCases.legalPage(unknownUserActor, LegalPageKind.TERMS_OF_SALE)
        assertThat(legalPage).isEqualTo(writtenTermsOfSale)
    }

    @Test
    fun `an unknown user should not publish a legal page`() {
        // Act
        val thrown = catchThrowable {
            legalPageUseCases.publish(unknownUserActor, LegalPageKind.LEGAL_NOTICE, LegalText("Mentions"))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(LegalPagePublicationForbiddenException::class.java)
    }

    @ParameterizedTest(name = "the text \"{0}\" is refused")
    @ValueSource(strings = ["", "  \n  "])
    fun `a legal page without text should be refused`(text: String) {
        // Act
        val thrown = catchThrowable {
            legalPageUseCases.publish(administrator, LegalPageKind.TERMS_OF_SALE, LegalText(text))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidLegalPageException::class.java)
        val legalPage = legalPageUseCases.legalPage(unknownUserActor, LegalPageKind.TERMS_OF_SALE)
        assertThat(legalPage).isEqualTo(writtenTermsOfSale)
    }

    @Test
    fun `a legal page of more than 50 000 characters should be refused`() {
        // Arrange
        val tooLongText = LegalText("a".repeat(50_001))
        // Act
        val thrown = catchThrowable {
            legalPageUseCases.publish(administrator, LegalPageKind.LEGAL_NOTICE, tooLongText)
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidLegalPageException::class.java)
    }

    @Test
    fun `a legal page of 50 000 characters should be published`() {
        // Arrange
        val longestText = LegalText("a".repeat(50_000))
        // Act
        val legalNotice = legalPageUseCases.publish(administrator, LegalPageKind.LEGAL_NOTICE, longestText)
        // Assert
        assertThat(legalNotice.text).isEqualTo(longestText)
    }
}
