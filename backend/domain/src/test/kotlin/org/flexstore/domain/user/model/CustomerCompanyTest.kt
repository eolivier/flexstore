package org.flexstore.domain.user.model

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CustomerCompanyTest : WithAssertions {

    private fun company(name: String, siren: String, vatNumber: String) =
        CustomerCompany(Company(CompanyName(name), VatNumber.from(vatNumber)), Siren.from(siren))

    @ParameterizedTest(name = "''{0}'', SIREN ''{1}'', VAT ''{2}'' is valid")
    @CsvSource(
        "Acme, 123456789, FR32123456789",
        "Acme, 123 456 789, fr 32 123 456 789",
        "Acme, 123456789, ''",
        "Acme, 123456789, DE123456789"
    )
    fun `a company with a name, a SIREN and maybe a VAT number should be valid`(
        name: String, siren: String, vatNumber: String
    ) {
        // Act
        val company = company(name, siren, vatNumber)
        // Assert
        assertThat(company.isValid()).isTrue()
    }

    @ParameterizedTest(name = "''{0}'', SIREN ''{1}'', VAT ''{2}'' is refused")
    @CsvSource(
        "'', 123456789, FR32123456789",
        "Acme, '', FR32123456789",
        "Acme, 12345678, ''",
        "Acme, 12345678A, ''",
        "Acme, 123456789, 123456789",
        "Acme, 123456789, FR32987654321"
    )
    fun `a company without name, with a malformed SIREN or a VAT number that does not agree should be refused`(
        name: String, siren: String, vatNumber: String
    ) {
        // Act
        val company = company(name, siren, vatNumber)
        // Assert
        assertThat(company.isValid()).isFalse()
    }

    @Test
    fun `a customer who buys for themselves should always be valid`() {
        // Act
        val individual = CustomerType.Individual
        // Assert
        assertThat(individual.isValid()).isTrue()
    }
}
