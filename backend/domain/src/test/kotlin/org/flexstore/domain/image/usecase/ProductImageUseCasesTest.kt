package org.flexstore.domain.image.usecase

import org.flexstore.domain.actor.unknownUserActor
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.image.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.repository.InMemoryProductImageRepository
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import org.flexstore.domain.user.*
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.unknownUserActor

class ProductImageUseCasesTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId("desk-lamp"), Name("Desk lamp"), price)
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val productImageRepository = InMemoryProductImageRepository()

    // A resizer that keeps as many bytes as the format allows
    private val truncatingResizer = object : ImageResizerRepository {
        override fun resize(content: ImageContent, format: ImageFormat): ImageContent {
            val bytes = content.bytes.value.copyOf(minOf(content.size(), format.maxBytes))
            return ImageContent(ImageBytes(bytes), ImageType.JPEG)
        }
    }

    // A resizer that does not reduce the image
    private val lazyResizer = object : ImageResizerRepository {
        override fun resize(content: ImageContent, format: ImageFormat) = content
    }

    private val administrator = testAdministratorActor

    private fun productImageUseCases(imageResizerRepository: ImageResizerRepository) = ProductImageUseCases(
        AddProductImageUseCase(productImageRepository, productRepository, imageResizerRepository),
        RemoveProductImageUseCase(productImageRepository),
        ReadProductImageUseCase(productImageRepository),
        ListProductsWithImageUseCase(productImageRepository)
    )

    private val productImageUseCases = productImageUseCases(truncatingResizer)

    private fun uploadedImageOf(size: Int, productId: ProductId = deskLamp.productId): UploadedImage {
        val content = ImageContent(ImageBytes(ByteArray(size) { 1 }), ImageType.PNG)
        return UploadedImage(productId, content)
    }

    @Test
    fun `an image should be kept in every format, within the size of each format`() {
        // Act
        productImageUseCases.add(administrator, uploadedImageOf(UploadedImage.MAX_BYTES))
        // Assert
        val large = productImageUseCases.getImage(unknownUserActor, deskLamp.productId, ImageFormat.LARGE)
        val small = productImageUseCases.getImage(unknownUserActor, deskLamp.productId, ImageFormat.SMALL)
        assertThat(large.content.size()).isEqualTo(ImageFormat.LARGE.maxBytes)
        assertThat(small.content.size()).isEqualTo(ImageFormat.SMALL.maxBytes)
        val productsWithImage = productImageUseCases.getProductsWithImage(unknownUserActor)
        assertThat(productsWithImage).isEqualTo(ProductIds(deskLamp.productId))
    }

    @Test
    fun `an image of more than 2 MB should be refused`() {
        // Arrange
        val tooLargeImage = uploadedImageOf(UploadedImage.MAX_BYTES + 1)
        // Act
        val thrown = catchThrowable { productImageUseCases.add(administrator, tooLargeImage) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ImageTooLargeException::class.java)
    }

    @Test
    fun `an image that was not reduced to its format should not be kept as such`() {
        // Arrange
        val lazyUseCases = productImageUseCases(lazyResizer)
        val largeImage = uploadedImageOf(ImageFormat.LARGE.maxBytes + 1)
        // Act
        val thrown = catchThrowable { lazyUseCases.add(administrator, largeImage) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductImageStorageFailed::class.java)
    }

    @Test
    fun `the image of an unknown product should be refused`() {
        // Arrange
        val unknownProductImage = uploadedImageOf(1000, ProductId("unknown"))
        // Act
        val thrown = catchThrowable { productImageUseCases.add(administrator, unknownProductImage) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductNotFoundException::class.java)
    }

    @Test
    fun `a removed image should no longer be found`() {
        // Arrange
        productImageUseCases.add(administrator, uploadedImageOf(1000))
        // Act
        productImageUseCases.remove(administrator, deskLamp.productId)
        // Assert
        val productId = deskLamp.productId
        val reader = unknownUserActor
        val thrown = catchThrowable { productImageUseCases.getImage(reader, productId, ImageFormat.SMALL) }
        assertThat(thrown).hasCauseInstanceOf(ProductImageNotFoundException::class.java)
        assertThat(productImageUseCases.getProductsWithImage(unknownUserActor)).isEqualTo(ProductIds())
    }

    @Test
    fun `a customer should not give the image of a product`() {
        // Act
        val thrown = catchThrowable { productImageUseCases.add(testCustomerActor, uploadedImageOf(1000)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductImageForbiddenException::class.java)
        assertThat(productImageRepository.productsWithImage()).doesNotContain(deskLamp.productId)
    }

    @Test
    fun `an unknown user should not remove the image of a product`() {
        // Arrange
        productImageUseCases.add(administrator, uploadedImageOf(1000))
        // Act
        val thrown = catchThrowable { productImageUseCases.remove(unknownUserActor, deskLamp.productId) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductImageForbiddenException::class.java)
    }
}
