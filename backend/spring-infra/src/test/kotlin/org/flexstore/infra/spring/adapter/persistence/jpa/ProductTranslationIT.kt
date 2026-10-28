package org.flexstore.infra.spring.adapter.persistence.jpa

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductTranslationEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaProductRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@ActiveProfiles("test")
@SpringBootTest
class ProductTranslationIT {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var jpaProductRepository: JpaProductRepository

    private val englishText = ProductText(Name("Translated lamp"), Description("Warm light"))
    private val frenchText = ProductText(Name("Lampe traduite"), Description("Lumière chaude"))
    private val frenchTranslation = ProductTranslation(Language.FR, frenchText)
    private val englishTranslation = ProductTranslation(Language.EN, englishText)
    private val translations = ProductTranslations(englishTranslation, frenchTranslation)
    private val price = Price(Amount(BigDecimal(30)), Currency.EUR)
    private val lampSku = Sku("TRANSLATED-LAMP")
    private val lamp = DefinedProduct(ProductId.nextId(), lampSku, translations, Category.HOME, price)

    // The Waterproof Jacket of the catalog
    private val jacketId = ProductId(Identity("2"))

    @AfterEach
    fun deleteLamp() {
        jpaProductRepository.deleteById(lamp.productId.id.value)
    }

    private fun storedFrenchRow(): ProductTranslationEntity {
        val storedLamp = jpaProductRepository.findById(lamp.productId.id.value).orElseThrow()
        return storedLamp.translations.single { it.language == "fr" }
    }

    @Test
    fun `a product of the catalog should be read with its French translation`() {
        // Act
        val jacket = productRepository.findById(jacketId)!!
        // Assert
        assertThat(jacket.name).isEqualTo(Name("Waterproof Jacket"))
        assertThat(jacket.translationIn(Language.FR).name).isEqualTo(Name("Veste imperméable"))
    }

    @Test
    fun `a product should be stored with its translations`() {
        // Act
        productRepository.save(lamp)
        // Assert
        assertThat(productRepository.findById(lamp.productId)!!.translations).isEqualTo(translations)
    }

    @Test
    fun `saving a product again should keep the row of its translation`() {
        // Arrange
        productRepository.save(lamp)
        val frenchRow = storedFrenchRow()
        val reworded = ProductText(Name("Lampe de chevet"), Description("Lumière chaude"))
        val rewordedTranslation = ProductTranslation(Language.FR, reworded)
        val rewordedLamp = lamp.copy(translations = translations.updatedWith(rewordedTranslation))
        // Act
        productRepository.save(rewordedLamp)
        // Assert
        val updatedRow = storedFrenchRow()
        assertThat(updatedRow.id).isEqualTo(frenchRow.id)
        assertThat(updatedRow.createdAt).isEqualTo(frenchRow.createdAt)
        assertThat(updatedRow.name).isEqualTo("Lampe de chevet")
    }

    @Test
    fun `a translation no longer given should be removed`() {
        // Arrange
        productRepository.save(lamp)
        val englishOnlyLamp = lamp.copy(translations = ProductTranslations.inDefaultLanguage(englishText))
        // Act
        productRepository.save(englishOnlyLamp)
        // Assert
        assertThat(productRepository.findById(lamp.productId)!!.translationIn(Language.FR).name)
            .isEqualTo(Name("Translated lamp"))
    }
}
