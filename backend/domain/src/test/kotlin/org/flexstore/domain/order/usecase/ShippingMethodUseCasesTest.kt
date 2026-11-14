package org.flexstore.domain.order.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.unknownUserActor
import org.flexstore.domain.order.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.repository.InMemoryShippingMethodRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ShippingMethodUseCasesTest : WithAssertions {

    private val shippingMethodRepository = InMemoryShippingMethodRepository()
    private val listShippingMethodsUseCase = ListShippingMethodsUseCase(shippingMethodRepository)
    private val createShippingMethodUseCase = CreateShippingMethodUseCase(shippingMethodRepository)
    private val updateShippingMethodUseCase = UpdateShippingMethodUseCase(shippingMethodRepository)

    private val relayPoint = ShippingMethod(
        ShippingMethodId.nextId(), ShippingLabel("Point relais"),
        Price(Amount(BigDecimal("3.50")), Currency.EUR), FreeShippingEligibility.ELIGIBLE
    )

    @Test
    fun `an administrator should add a shipping method, which is then read with the others`() {
        // Act
        createShippingMethodUseCase.unfold(ShippingMethodCreation(testAdministratorActor, relayPoint))
        // Assert
        val shippingMethods = listShippingMethodsUseCase.unfold(ShippingMethodsReading(testAdministratorActor))
        assertThat(shippingMethods.asList()).contains(relayPoint)
        assertThat(shippingMethods.asList()).hasSize(3)
    }

    @Test
    fun `the shipping methods should be read from the cheapest`() {
        // Arrange
        createShippingMethodUseCase.unfold(ShippingMethodCreation(testAdministratorActor, relayPoint))
        // Act
        val shippingMethods = listShippingMethodsUseCase.unfold(ShippingMethodsReading(testAdministratorActor))
        // Assert
        assertThat(shippingMethods.asList().first()).isEqualTo(relayPoint)
    }

    @Test
    fun `a shipping method without name should be refused`() {
        // Arrange
        val unnamed = relayPoint.copy(label = ShippingLabel(""))
        // Act
        val thrown = catchThrowable {
            createShippingMethodUseCase.unfold(ShippingMethodCreation(testAdministratorActor, unnamed))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidShippingMethodException::class.java)
    }

    @Test
    fun `an administrator should change the cost of a shipping method`() {
        // Arrange
        val dearerStandard = ShippingMethod.STANDARD.copy(cost = Price(Amount(BigDecimal("5.90")), Currency.EUR))
        // Act
        updateShippingMethodUseCase.unfold(ShippingMethodUpdate(testAdministratorActor, dearerStandard))
        // Assert
        assertThat(shippingMethodRepository.get(ShippingMethod.STANDARD.id)).isEqualTo(dearerStandard)
    }

    @Test
    fun `an unknown shipping method should not be changed`() {
        // Act
        val thrown = catchThrowable {
            updateShippingMethodUseCase.unfold(ShippingMethodUpdate(testAdministratorActor, relayPoint))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ShippingMethodNotFoundException::class.java)
    }

    @Test
    fun `an administrator should disable a shipping method while another one stays enabled`() {
        // Arrange
        val disabledExpress = ShippingMethod.EXPRESS.copy(status = ShippingMethodStatus.DISABLED)
        // Act
        updateShippingMethodUseCase.unfold(ShippingMethodUpdate(testAdministratorActor, disabledExpress))
        // Assert
        assertThat(shippingMethodRepository.get(ShippingMethod.EXPRESS.id).isDisabled()).isTrue()
    }

    @Test
    fun `the last enabled shipping method should not be disabled`() {
        // Arrange
        val disabledExpress = ShippingMethod.EXPRESS.copy(status = ShippingMethodStatus.DISABLED)
        updateShippingMethodUseCase.unfold(ShippingMethodUpdate(testAdministratorActor, disabledExpress))
        val disabledStandard = ShippingMethod.STANDARD.copy(status = ShippingMethodStatus.DISABLED)
        // Act
        val thrown = catchThrowable {
            updateShippingMethodUseCase.unfold(ShippingMethodUpdate(testAdministratorActor, disabledStandard))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(NoShippingMethodLeftException::class.java)
        assertThat(shippingMethodRepository.get(ShippingMethod.STANDARD.id).isEnabled()).isTrue()
    }

    @Test
    fun `a customer and an unknown user should not manage the shipping methods`() {
        // Act
        val customerThrown = catchThrowable {
            createShippingMethodUseCase.unfold(ShippingMethodCreation(testCustomerActor, relayPoint))
        }
        val unknownThrown = catchThrowable {
            updateShippingMethodUseCase.unfold(ShippingMethodUpdate(unknownUserActor, ShippingMethod.STANDARD))
        }
        val readingThrown = catchThrowable {
            listShippingMethodsUseCase.unfold(ShippingMethodsReading(testCustomerActor))
        }
        // Assert
        assertThat(customerThrown).hasCauseInstanceOf(ShippingMethodAdministrationForbiddenException::class.java)
        assertThat(unknownThrown).hasCauseInstanceOf(ShippingMethodAdministrationForbiddenException::class.java)
        assertThat(readingThrown).hasCauseInstanceOf(ShippingMethodAdministrationForbiddenException::class.java)
    }
}
