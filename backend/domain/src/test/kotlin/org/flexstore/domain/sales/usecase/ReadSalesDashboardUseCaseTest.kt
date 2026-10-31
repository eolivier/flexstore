package org.flexstore.domain.sales.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.order.*
import org.flexstore.domain.sales.*
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Test
import java.time.Instant

class ReadSalesDashboardUseCaseTest : WithAssertions {

    private val useCase = ReadSalesDashboardUseCase(InMemoryOrderRepository())
    private val now = Instant.parse("2026-10-31T10:00:00Z")

    @Test
    fun `the dashboard of a period should end the day it is asked`() {
        // Act
        val dashboard = useCase.unfold(SalesQuestion(DayCount(30), now))
        // Assert
        assertThat(dashboard.dailySales.asList()).hasSize(30)
        assertThat(dashboard.orderCount).isEqualTo(OrderCount(0))
    }

    @Test
    fun `a period of another number of days should be refused`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(SalesQuestion(DayCount(10), now)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSalesPeriodException::class.java)
    }
}
