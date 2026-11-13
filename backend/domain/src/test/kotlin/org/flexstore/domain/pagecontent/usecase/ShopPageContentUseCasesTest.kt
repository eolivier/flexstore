package org.flexstore.domain.pagecontent.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.pagecontent.*
import org.flexstore.domain.pagecontent.ShopPageContentType.*
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.unknownUserActor
import org.flexstore.infra.repository.InMemoryShopPageContentRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ShopPageContentUseCasesTest : WithAssertions {

    private val shopDescription = ShopPageContent(META_SHOP_DESCRIPTION, ShopPageContentText("Demo shop"))
    private val shopPageContentRepository = InMemoryShopPageContentRepository(ShopPageContents(shopDescription))
    private val shopPageContentUseCases = ShopPageContentUseCases(
        ReadShopPageContentUseCase(shopPageContentRepository),
        EditShopPageContentUseCase(shopPageContentRepository)
    )

    private fun contentOf(type: ShopPageContentType, text: String) =
        ShopPageContent(type, ShopPageContentText(text))

    @Test
    fun `a content never written should be read empty`() {
        // Act
        val contents = shopPageContentUseCases.contents()
        // Assert
        assertThat(contents.of(META_KEY_WORDS).text).isEqualTo(ShopPageContentText.empty())
    }

    @Test
    fun `an administrator should edit the content of a type and leave the others as they were`() {
        // Arrange
        val keyWords = contentOf(META_KEY_WORDS, "shop, demo")
        // Act
        val contents = shopPageContentUseCases.edit(testAdministratorActor, keyWords)
        // Assert
        assertThat(contents.of(META_KEY_WORDS)).isEqualTo(keyWords)
        assertThat(contents.of(META_SHOP_DESCRIPTION)).isEqualTo(shopDescription)
    }

    @Test
    fun `an administrator should clear a content with an empty text`() {
        // Arrange
        val clearedDescription = contentOf(META_SHOP_DESCRIPTION, "")
        // Act
        val contents = shopPageContentUseCases.edit(testAdministratorActor, clearedDescription)
        // Assert
        assertThat(contents.of(META_SHOP_DESCRIPTION)).isEqualTo(clearedDescription)
    }

    @Test
    fun `a customer should not edit a content`() {
        // Act
        val thrown = catchThrowable {
            shopPageContentUseCases.edit(testCustomerActor, contentOf(META_KEY_WORDS, "shop"))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ShopPageContentForbiddenException::class.java)
        assertThat(shopPageContentUseCases.contents().of(META_KEY_WORDS).text.isWritten()).isFalse()
    }

    @Test
    fun `an unknown user should not edit a content`() {
        // Act
        val thrown = catchThrowable {
            shopPageContentUseCases.edit(unknownUserActor, contentOf(META_KEY_WORDS, "shop"))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ShopPageContentForbiddenException::class.java)
    }

    @Test
    fun `a text of more than 1 000 characters should be refused`() {
        // Arrange
        val tooLongKeyWords = contentOf(META_KEY_WORDS, "a".repeat(1_001))
        // Act
        val thrown = catchThrowable { shopPageContentUseCases.edit(testAdministratorActor, tooLongKeyWords) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidShopPageContentException::class.java)
    }

    @Test
    fun `a text of 1 000 characters should be saved`() {
        // Arrange
        val longestKeyWords = contentOf(META_KEY_WORDS, "a".repeat(1_000))
        // Act
        val contents = shopPageContentUseCases.edit(testAdministratorActor, longestKeyWords)
        // Assert
        assertThat(contents.of(META_KEY_WORDS)).isEqualTo(longestKeyWords)
    }

    @ParameterizedTest(name = "the identifier \"{0}\" is refused")
    @ValueSource(strings = ["G-123 456", "\"><script>", "G-1234;alert(1)"])
    fun `a Google Analytics identifier with other characters than letters digits and hyphens should be refused`(
        identifier: String
    ) {
        // Act
        val thrown = catchThrowable {
            shopPageContentUseCases.edit(testAdministratorActor, contentOf(GOOGLE_ANALYTICS_ID, identifier))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidShopPageContentException::class.java)
    }

    @Test
    fun `a Google Analytics identifier should be saved`() {
        // Arrange
        val analyticsId = contentOf(GOOGLE_ANALYTICS_ID, "G-ABC123DEF4")
        // Act
        val contents = shopPageContentUseCases.edit(testAdministratorActor, analyticsId)
        // Assert
        assertThat(contents.of(GOOGLE_ANALYTICS_ID)).isEqualTo(analyticsId)
    }
}
