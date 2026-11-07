package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.domain.pagecontent.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShopPageContentEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaShopPageContentRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.PostgresShopPageContentRepositoryAdapter
import org.flexstore.infra.spring.config.CacheConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.function.Supplier

// The cache of the read of the shop page content table, with the real adapter behind the Spring cache, without a
// database
class ShopPageContentReadCacheTest {

    private val keyWordsEntity = ShopPageContentEntity("content-1", "META_KEY_WORDS", "shop, demo")
    private val newKeyWords = ShopPageContent(ShopPageContentType.META_KEY_WORDS, ShopPageContentText("shop"))

    private val jpaShopPageContentRepository = mock(JpaShopPageContentRepository::class.java)
    private lateinit var context: AnnotationConfigApplicationContext
    private lateinit var shopPageContentRepository: ShopPageContentRepository

    @BeforeEach
    fun startSpring() {
        `when`(jpaShopPageContentRepository.findAll()).thenReturn(listOf(keyWordsEntity))
        `when`(jpaShopPageContentRepository.findByPageType("META_KEY_WORDS")).thenReturn(keyWordsEntity)
        context = AnnotationConfigApplicationContext()
        context.register(CacheConfig::class.java, PostgresShopPageContentRepositoryAdapter::class.java)
        context.registerBean(JpaShopPageContentRepository::class.java, Supplier { jpaShopPageContentRepository })
        context.refresh()
        shopPageContentRepository = context.getBean(ShopPageContentRepository::class.java)
    }

    @AfterEach
    fun stopSpring() {
        context.close()
    }

    @Test
    fun `reading the contents twice should read the table once`() {
        // Act
        shopPageContentRepository.all()
        shopPageContentRepository.all()
        // Assert
        verify(jpaShopPageContentRepository, times(1)).findAll()
    }

    @Test
    fun `an edit should evict the cached contents`() {
        // Arrange
        shopPageContentRepository.all()
        shopPageContentRepository.save(newKeyWords)
        clearInvocations(jpaShopPageContentRepository)
        // Act
        shopPageContentRepository.all()
        // Assert
        verify(jpaShopPageContentRepository, times(1)).findAll()
    }
}
