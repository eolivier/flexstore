package org.flexstore.domain.email

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.ShopUrl
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VerificationPageTest : WithAssertions {

    @ParameterizedTest(name = "the shop {0} should verify the emails on its page /verify-email")
    @ValueSource(strings = ["https://flexstore.example", "https://flexstore.example/"])
    fun `the verification page should be a page of the shop`(shopAddress: String) {
        // Act
        val verificationPage = VerificationPage.of(ShopUrl(shopAddress))
        // Assert
        assertThat(verificationPage).isEqualTo(VerificationPage("https://flexstore.example/verify-email"))
    }
}
