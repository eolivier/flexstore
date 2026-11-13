package org.flexstore.domain.discount.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.discount.*
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryDiscountCodeRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.unknownUserActor

class DiscountCodeUseCasesTest : WithAssertions {

    private val discountCodeRepository = InMemoryDiscountCodeRepository()
    private val discountCodeUseCases = DiscountCodeUseCases(
        CreateDiscountCodeUseCase(discountCodeRepository),
        ListDiscountCodesUseCase(discountCodeRepository),
        DeleteDiscountCodeUseCase(discountCodeRepository)
    )
    private val welcome10 = DiscountCode(
        PromoCode("WELCOME10"),
        DiscountOffer(
            PercentageOff(Percentage(BigDecimal(10))),
            DiscountValidity(ExpiryDate(Instant.parse("2026-12-31T23:00:00Z")), UsageLimit(100))
        )
    )

    @Test
    fun `an administrator should read the discount codes and delete one`() {
        // Arrange
        discountCodeUseCases.create(testAdministratorActor, welcome10)
        // Act
        discountCodeUseCases.delete(testAdministratorActor, welcome10.code)
        // Assert
        assertThat(discountCodeUseCases.getAll(testAdministratorActor).asList()).isEmpty()
    }

    @Test
    fun `a customer should not read the discount codes`() {
        // Act
        val thrown = catchThrowable { discountCodeUseCases.getAll(testCustomerActor) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
    }

    @Test
    fun `a customer should not delete a discount code`() {
        // Arrange
        discountCodeUseCases.create(testAdministratorActor, welcome10)
        // Act
        val thrown = catchThrowable { discountCodeUseCases.delete(testCustomerActor, welcome10.code) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
        assertThat(discountCodeRepository.codeExists(welcome10.code)).isTrue()
    }

    @Test
    fun `an unknown user should not delete a discount code`() {
        // Arrange
        discountCodeUseCases.create(testAdministratorActor, welcome10)
        // Act
        val thrown = catchThrowable { discountCodeUseCases.delete(unknownUserActor, welcome10.code) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
    }
}
