package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.SellerEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.seller.JpaSellerRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.seller.PostgresSellerRepositoryAdapter
import org.flexstore.infra.spring.config.CacheConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.Optional
import java.util.function.Supplier

// The cache of the reads of the seller table, with the real adapter behind the Spring cache, without a database
class SellerReadCacheTest {

    private val sellerId = SellerId(Identity("seller-1"))
    private val sellerEntity = sellerEntity(sellerId.asString(), 1)
    private val newSeller = Seller(
        sellerId,
        SellerDetail(
            SellerContact(SellerName("Flexstore SAS"), SellerAddress("1 rue de la Démo, 75001 Paris")),
            SellerIdentifier(Siret("123 456 789 00012"), SellerVatNumber("FR12345678901")),
            SellerReach(SellerPhone(""), SellerEmail(""), SellerWebsite("")),
            SellerIncorporation(SellerLegalForm(""), ShareCapital(""), RegistryCity(""))
        )
    )

    private val jpaSellerRepository = mock(JpaSellerRepository::class.java)
    private lateinit var context: AnnotationConfigApplicationContext
    private lateinit var sellerRepository: SellerRepository

    @BeforeEach
    fun startSpring() {
        `when`(jpaSellerRepository.findFirstByOrderByVersionDesc()).thenReturn(sellerEntity)
        `when`(jpaSellerRepository.findById(sellerId.asString())).thenReturn(Optional.of(sellerEntity))
        context = AnnotationConfigApplicationContext()
        context.register(CacheConfig::class.java, PostgresSellerRepositoryAdapter::class.java)
        context.registerBean(JpaSellerRepository::class.java, Supplier { jpaSellerRepository })
        context.refresh()
        sellerRepository = context.getBean(SellerRepository::class.java)
    }

    @AfterEach
    fun stopSpring() {
        context.close()
    }

    @Test
    fun `reading the current seller twice should read the table once`() {
        // Act
        sellerRepository.current()
        sellerRepository.current()
        // Assert
        verify(jpaSellerRepository, times(1)).findFirstByOrderByVersionDesc()
    }

    @Test
    fun `reading a seller by id twice should read the table once`() {
        // Act
        sellerRepository.findBy(sellerId)
        sellerRepository.findBy(sellerId)
        // Assert
        verify(jpaSellerRepository, times(1)).findById(sellerId.asString())
    }

    @Test
    fun `a save should evict the current seller`() {
        // Arrange
        sellerRepository.current()
        sellerRepository.save(newSeller)
        clearInvocations(jpaSellerRepository)
        // Act
        sellerRepository.current()
        // Assert
        verify(jpaSellerRepository, times(1)).findFirstByOrderByVersionDesc()
    }

    @Test
    fun `a save should evict the seller read by id`() {
        // Arrange
        sellerRepository.findBy(sellerId)
        sellerRepository.save(newSeller)
        clearInvocations(jpaSellerRepository)
        // Act
        sellerRepository.findBy(sellerId)
        // Assert
        verify(jpaSellerRepository, times(1)).findById(sellerId.asString())
    }

    private fun sellerEntity(id: String, version: Int) = SellerEntity(
        id, version, "Flexstore SAS", "1 rue de la Démo, 75001 Paris", "123 456 789 00012", "FR12345678901",
        null, null, null, null, null, null, null, null, null, null, null, null
    )
}
