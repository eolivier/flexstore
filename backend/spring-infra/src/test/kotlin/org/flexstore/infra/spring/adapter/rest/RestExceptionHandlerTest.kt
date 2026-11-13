package org.flexstore.infra.spring.adapter.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.image.ProductImageReadingForbiddenException
import org.flexstore.domain.legal.LegalPageReadingForbiddenException
import org.flexstore.domain.pagecontent.ShopPageContentReadingForbiddenException
import org.flexstore.domain.product.model.ProductReadingForbiddenException
import org.flexstore.domain.review.ReviewReadingForbiddenException
import org.flexstore.domain.seller.SellerReadingForbiddenException
import org.flexstore.domain.settings.ShopSettingsReadingForbiddenException
import org.flexstore.domain.stock.StockReadingForbiddenException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpStatus
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.PreConditionException

class RestExceptionHandlerTest {

    private val handler = RestExceptionHandler()

    @ParameterizedTest(name = "{0}")
    @MethodSource("readingForbiddenExceptions")
    fun `a refused reader should be answered with 403 and the message of the refusal`(
        forbidden: PreConditionException
    ) {
        // Act
        val answer = handler.handlePreConditionException(forbidden)
        // Assert
        assertThat(answer.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(answer.body).isEqualTo(mapOf("error" to "Refused."))
    }

    @Test
    fun `a refusal raised by a use case should be answered with the status of its precondition`() {
        // Arrange
        val nominal = NominalException(SellerReadingForbiddenException(REFUSAL))
        // Act
        val answer = handler.handleNominalException(nominal)
        // Assert
        assertThat(answer.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    companion object {
        private val REFUSAL = ErrorMessage("Refused.")

        @JvmStatic
        fun readingForbiddenExceptions() = listOf(
            SellerReadingForbiddenException(REFUSAL),
            ReviewReadingForbiddenException(REFUSAL),
            LegalPageReadingForbiddenException(REFUSAL),
            ProductImageReadingForbiddenException(REFUSAL),
            ShopSettingsReadingForbiddenException(REFUSAL),
            ShopPageContentReadingForbiddenException(REFUSAL),
            ProductReadingForbiddenException(REFUSAL),
            StockReadingForbiddenException(REFUSAL),
        )
    }
}
