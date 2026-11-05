package org.flexstore.domain.discount.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.discount.*
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryDiscountCodeRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.unknownUser
import org.flexstore.domain.user.administration.usersOfTheAdministration

class DiscountCodeUseCasesTest : WithAssertions {

    private val discountCodeRepository = InMemoryDiscountCodeRepository()
    private val userRepository = usersOfTheAdministration()
    private val discountCodeUseCases = DiscountCodeUseCases(
        CreateDiscountCodeUseCase(discountCodeRepository, userRepository),
        ListDiscountCodesUseCase(discountCodeRepository, userRepository),
        DeleteDiscountCodeUseCase(discountCodeRepository, userRepository)
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
        discountCodeUseCases.create(testAdministrator.id, welcome10)
        // Act
        discountCodeUseCases.delete(testAdministrator.id, welcome10.code)
        // Assert
        assertThat(discountCodeUseCases.getAll(testAdministrator.id).asList()).isEmpty()
    }

    @Test
    fun `a customer should not read the discount codes`() {
        // Act
        val thrown = catchThrowable { discountCodeUseCases.getAll(testCustomer.id) }
        // Assert
        assertThat(thrown).isInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
    }

    @Test
    fun `a customer should not delete a discount code`() {
        // Arrange
        discountCodeUseCases.create(testAdministrator.id, welcome10)
        // Act
        val thrown = catchThrowable { discountCodeUseCases.delete(testCustomer.id, welcome10.code) }
        // Assert
        assertThat(thrown).isInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
        assertThat(discountCodeRepository.codeExists(welcome10.code)).isTrue()
    }

    @Test
    fun `an unknown user should not delete a discount code`() {
        // Arrange
        discountCodeUseCases.create(testAdministrator.id, welcome10)
        // Act
        val thrown = catchThrowable { discountCodeUseCases.delete(unknownUser, welcome10.code) }
        // Assert
        assertThat(thrown).isInstanceOf(DiscountCodeAdministrationForbiddenException::class.java)
    }
}
