package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductImageEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaProductImageRepository : JpaRepository<ProductImageEntity, String> {
    fun findByProductIdAndFormat(productId: String, format: String): ProductImageEntity?

    @Query("SELECT DISTINCT i.productId FROM ProductImageEntity i")
    fun findProductIds(): List<String>

    @Modifying
    fun deleteAllByProductId(productId: String)
}

@Repository
@Transactional
class PostgresProductImageRepositoryAdapter(private val jpaRepo: JpaProductImageRepository) :
    ProductImageRepository {

    // The row of the format is replaced: its id and its creation date are kept
    override fun save(productImage: ProductImage) {
        val productId = productImage.productId.id.value
        val storedImage = jpaRepo.findByProductIdAndFormat(productId, productImage.format.name)
        val content = productImage.content
        val imageEntity = ProductImageEntity(
            storedImage?.id ?: Identity.nextId().value,
            productId = productId,
            format = productImage.format.name,
            mediaType = content.type.mediaType,
            content = content.bytes.value
        )
        jpaRepo.save(imageEntity.also { it.createdAt = storedImage?.createdAt })
    }

    @Transactional(readOnly = true)
    override fun find(productId: ProductId, format: ImageFormat): ProductImage? =
        jpaRepo.findByProductIdAndFormat(productId.id.value, format.name)?.toDomain()

    @Transactional(readOnly = true)
    override fun productsWithImage() = jpaRepo.findProductIds().map { ProductId(Identity(it)) }

    override fun deleteAllOf(productId: ProductId) {
        jpaRepo.deleteAllByProductId(productId.id.value)
    }

    private fun ProductImageEntity.toDomain(): ProductImage {
        val imageType = ImageType.ofMediaType(mediaType) ?: ImageType.JPEG
        val imageContent = ImageContent(ImageBytes(content), imageType)
        return ProductImage(ProductId(Identity(productId)), ImageFormat.valueOf(format), imageContent)
    }
}
