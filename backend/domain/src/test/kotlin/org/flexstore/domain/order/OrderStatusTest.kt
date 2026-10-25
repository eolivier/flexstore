package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class OrderStatusTest : WithAssertions {

    @ParameterizedTest(name = "an order {0} should then be {1}")
    @CsvSource("AWAITING_PAYMENT, PAID", "PAID, PROCESSING", "PROCESSING, SHIPPED", "SHIPPED, DELIVERED")
    fun `an order should go through its life cycle`(status: OrderStatus, nextStatus: OrderStatus) {
        // Act
        val next = status.next()
        // Assert
        assertThat(next).isEqualTo(nextStatus)
    }

    @ParameterizedTest(name = "an order {0} is moved by an administrator: {1}")
    @CsvSource("AWAITING_PAYMENT, false", "PAID, true", "PROCESSING, true", "SHIPPED, true", "DELIVERED, false")
    fun `only the payment should move an order awaiting it`(status: OrderStatus, byAnAdministrator: Boolean) {
        // Act
        val movedByAnAdministrator = status.isMovedByAnAdministrator()
        // Assert
        assertThat(movedByAnAdministrator).isEqualTo(byAnAdministrator)
    }

    @Test
    fun `a delivered order should have no next status`() {
        // Act
        val thrown = catchThrowable { OrderStatus.DELIVERED.next() }
        // Assert
        assertThat(thrown).isInstanceOf(OrderStatusCannotAdvanceException::class.java)
    }
}
