package org.flexstore.domain.order.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.order.model.EnabledShippingMethodLookup
import org.flexstore.domain.order.model.ShippingMethodId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryShippingMethodRepository
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

// The reads of a customer when checking out: the shipping options of their cart and the shipping method they chose
class CustomerShippingUseCasesTest : WithAssertions {

    private val userRepository = customersWithIds("user-1")
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val customer = actorResolutionService.actorOf(UserId.ValidUserId("user-1"))
    private val proposeShippingOptionsUseCase = ProposeShippingOptionsUseCase(
        InMemoryCartRepository(), InMemoryShippingMethodRepository(), InMemoryShopSettingsRepository()
    )
    private val readEnabledShippingMethodUseCase = ReadEnabledShippingMethodUseCase(InMemoryShippingMethodRepository())
    private val someShippingMethod = ShippingMethodId(Identity("standard"))

    @Test
    fun `a customer should be proposed the shipping options of their empty cart`() {
        // Act
        val shippingOptions = proposeShippingOptionsUseCase.unfold(ShippingOptionsReading(customer))
        // Assert
        assertThat(shippingOptions).isNotNull()
    }

    @Test
    fun `an unknown customer should not be proposed shipping options`() {
        // Arrange
        val unknownCustomer = actorResolutionService.actorOf(unknownCustomerId)
        // Act
        val thrown = catchThrowable { proposeShippingOptionsUseCase.unfold(ShippingOptionsReading(unknownCustomer)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not be proposed shipping options`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val blockedCustomer = actorResolutionService.actorOf(blockedCustomerId)
        // Act
        val thrown = catchThrowable { proposeShippingOptionsUseCase.unfold(ShippingOptionsReading(blockedCustomer)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }

    @Test
    fun `an unknown customer should not read a shipping method`() {
        // Arrange
        val unknownCustomer = actorResolutionService.actorOf(unknownCustomerId)
        val lookup = EnabledShippingMethodLookup(unknownCustomer, someShippingMethod)
        // Act
        val thrown = catchThrowable { readEnabledShippingMethodUseCase.unfold(lookup) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not read a shipping method`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val blockedCustomer = actorResolutionService.actorOf(blockedCustomerId)
        val lookup = EnabledShippingMethodLookup(blockedCustomer, someShippingMethod)
        // Act
        val thrown = catchThrowable { readEnabledShippingMethodUseCase.unfold(lookup) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
