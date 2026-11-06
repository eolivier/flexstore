package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductTranslationEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaProductRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.PostgresProductRepositoryAdapter
import org.flexstore.infra.spring.config.CacheConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.util.Optional
import java.util.function.Supplier
import kotlin.test.assertNull

// The cache of the reads of the product table, with the real adapter behind the Spring cache, without a database
class ProductReadCacheTest {

    private val lampId = ProductId(Identity("product-1"))
    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val lampEntity = ProductEntity(
        id = lampId.id.value,
        sku = "HOM-1",
        category = Category.HOME.name,
        price = BigDecimal(35),
        currency = Currency.EUR.name,
        vatRate = VatRate.STANDARD.name,
        translations = listOf(ProductTranslationEntity("translation-1", "en", "Lamp", "A lamp"))
    )
    private val updatedLamp = DefinedProduct(
        lampId, Sku.from("HOM-1"), Name("Brighter lamp"), Description("A lamp"), Category.HOME, price
    )

    // The catalog order of the adapter: the oldest products first
    private val catalogOrder = Sort.by("createdAt", "id")

    private val jpaProductRepository = mock(JpaProductRepository::class.java)
    private lateinit var context: AnnotationConfigApplicationContext
    private lateinit var productRepository: ProductRepository

    @BeforeEach
    fun startSpring() {
        `when`(jpaProductRepository.findAll(any(Sort::class.java))).thenReturn(listOf(lampEntity))
        `when`(jpaProductRepository.findAllByCategory(Category.HOME.name, catalogOrder))
            .thenReturn(listOf(lampEntity))
        `when`(jpaProductRepository.findById(lampId.id.value)).thenReturn(Optional.of(lampEntity))
        `when`(jpaProductRepository.save(any(ProductEntity::class.java))).thenAnswer { it.arguments[0] }
        context = AnnotationConfigApplicationContext()
        context.register(CacheConfig::class.java, PostgresProductRepositoryAdapter::class.java)
        context.registerBean(JpaProductRepository::class.java, Supplier { jpaProductRepository })
        context.refresh()
        productRepository = context.getBean(ProductRepository::class.java)
    }

    @AfterEach
    fun stopSpring() {
        context.close()
    }

    @Test
    fun `reading the catalog twice should read the table once`() {
        // Act
        productRepository.findAll()
        productRepository.findAll()
        // Assert
        verify(jpaProductRepository, times(1)).findAll(any(Sort::class.java))
    }

    @Test
    fun `reading a product by id twice should read the table once`() {
        // Act
        productRepository.findById(lampId)
        productRepository.findById(lampId)
        // Assert
        verify(jpaProductRepository, times(1)).findById(lampId.id.value)
    }

    @Test
    fun `reading the products of a category twice should read the table once`() {
        // Act
        productRepository.findAllBy(Category.HOME)
        productRepository.findAllBy(Category.HOME)
        // Assert
        verify(jpaProductRepository, times(1)).findAllByCategory(Category.HOME.name, catalogOrder)
    }

    @Test
    fun `a missing product should be read as missing`() {
        // Act
        val product = productRepository.findById(ProductId(Identity("missing")))
        // Assert
        assertNull(product)
    }

    @Test
    fun `a save should evict the catalog`() {
        // Arrange
        productRepository.findAll()
        productRepository.save(updatedLamp)
        clearInvocations(jpaProductRepository)
        // Act
        productRepository.findAll()
        // Assert
        verify(jpaProductRepository, times(1)).findAll(any(Sort::class.java))
    }

    @Test
    fun `a save should evict the product by id`() {
        // Arrange
        productRepository.findById(lampId)
        productRepository.save(updatedLamp)
        clearInvocations(jpaProductRepository)
        // Act
        productRepository.findById(lampId)
        // Assert
        verify(jpaProductRepository, times(1)).findById(lampId.id.value)
    }

    @Test
    fun `a save should evict the products of a category`() {
        // Arrange
        productRepository.findAllBy(Category.HOME)
        productRepository.save(updatedLamp)
        clearInvocations(jpaProductRepository)
        // Act
        productRepository.findAllBy(Category.HOME)
        // Assert
        verify(jpaProductRepository, times(1)).findAllByCategory(Category.HOME.name, catalogOrder)
    }
}
