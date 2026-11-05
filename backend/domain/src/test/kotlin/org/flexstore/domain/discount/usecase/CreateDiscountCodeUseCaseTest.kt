package org.flexstore.domain.discount.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.discount.*
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryDiscountCodeRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.unknownUser
import org.flexstore.domain.user.administration.usersOfTheAdministration

class CreateDiscountCodeUseCaseTest : WithAssertions {

    private val discountCodeRepository = InMemoryDiscountCodeRepository()
    private val useCase = CreateDiscountCodeUseCase(discountCodeRepository, usersOfTheAdministration())
    private val endOfYear = ExpiryDate(Instant.parse("2026-12-31T23:00:00Z"))

    private fun discountCode(code: String, percent: Int, usageLimit: Int): DiscountCode {
        val reduction = PercentageOff(Percentage(BigDecimal(percent)))
        val offer = DiscountOffer(reduction, DiscountValidity(endOfYear, UsageLimit(usageLimit)))
        return DiscountCode(PromoCode(code), offer)
    }

    // The discount code given by the administrator of the tests
    private fun creationOf(code: String, percent: Int, usageLimit: Int) =
        DiscountCodeCreation(testAdministrator.id, discountCode(code, percent, usageLimit))

    @Test
    fun `an administrator should give a discount code, used by no order yet`() {
        // Act
        useCase.unfold(creationOf("WELCOME10", 10, 100))
        // Assert
        assertThat(discountCodeRepository.findBy(PromoCode("WELCOME10"))?.uses).isEqualTo(UseCount.none())
    }

    @ParameterizedTest(name = "the code {0}, {1} % off, for {2} orders is refused")
    @CsvSource("AB, 10, 1", "WELCOME-10, 10, 1", "WELCOME10, 0, 1", "WELCOME10, 101, 1", "WELCOME10, 10, 0")
    fun `an invalid discount code should be refused`(code: String, percent: Int, usageLimit: Int) {
        // Act
        val thrown = catchThrowable { useCase.unfold(creationOf(code, percent, usageLimit)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidDiscountCodeException::class.java)
    }

    @Test
    fun `a code already given should be refused`() {
        // Arrange
        useCase.unfold(creationOf("WELCOME10", 10, 100))
        // Act
        val thrown = catchThrowable { useCase.unfold(creationOf("WELCOME10", 20, 5)) }
        // Assert
        assertThat(thrown).isInstanceOf(DiscountCodeAlreadyExistsException::class.java)
    }

    @Test
    fun `a customer should not give a discount code`() {
        // Arrange
        val creation = DiscountCodeCreation(testCustomer.id, discountCode("WELCOME10", 10, 100))
        // Act
        val thrown = catchThrowable { useCase.unfold(creation) }
        // Assert
        assertThat(thrown).isInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
        assertThat(discountCodeRepository.codeIsMissing(PromoCode("WELCOME10"))).isTrue()
    }

    @Test
    fun `an unknown user should not give a discount code`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(DiscountCodeCreation(unknownUser, discountCode("WELCOME10", 10, 100))) }
        // Assert
        assertThat(thrown).isInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
    }
}
