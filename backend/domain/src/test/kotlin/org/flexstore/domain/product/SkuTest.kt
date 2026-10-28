package org.flexstore.domain.product

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SkuTest : WithAssertions {

    @Test
    fun `a typed SKU should be trimmed and put in upper case`() {
        // Act
        val sku = Sku.from("  tshirt-organic-m ")
        // Assert
        assertThat(sku).isEqualTo(Sku("TSHIRT-ORGANIC-M"))
    }

    @ParameterizedTest(name = "the SKU \"{0}\" is well formed")
    @ValueSource(strings = ["A", "CLO-1", "TSHIRT-ORGANIC-M", "123", "A23456789-123456789-123456789-12"])
    fun `upper-case letters and digits in groups separated by hyphens should be well formed`(value: String) {
        // Act
        val wellFormed = Sku(value).isWellFormed()
        // Assert
        assertThat(wellFormed).isTrue()
    }
}
