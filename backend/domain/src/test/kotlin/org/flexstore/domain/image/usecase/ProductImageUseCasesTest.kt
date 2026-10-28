package org.flexstore.domain.image.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.image.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.repository.InMemoryProductImageRepository
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProductImageUseCasesTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
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
        productImageUseCases.add(uploadedImageOf(UploadedImage.MAX_BYTES))
        // Assert
        val large = productImageUseCases.getImage(deskLamp.productId, ImageFormat.LARGE)
        val small = productImageUseCases.getImage(deskLamp.productId, ImageFormat.SMALL)
        assertThat(large.content.size()).isEqualTo(ImageFormat.LARGE.maxBytes)
        assertThat(small.content.size()).isEqualTo(ImageFormat.SMALL.maxBytes)
        assertThat(productImageUseCases.getProductsWithImage()).containsExactly(deskLamp.productId)
    }

    @Test
    fun `an image of more than 2 MB should be refused`() {
        // Arrange
        val tooLargeImage = uploadedImageOf(UploadedImage.MAX_BYTES + 1)
        // Act
        val thrown = catchThrowable { productImageUseCases.add(tooLargeImage) }
        // Assert
        assertThat(thrown).isInstanceOf(ImageTooLargeException::class.java)
    }

    @Test
    fun `an image that was not reduced to its format should not be kept as such`() {
        // Arrange
        val lazyUseCases = productImageUseCases(lazyResizer)
        // Act
        val thrown = catchThrowable { lazyUseCases.add(uploadedImageOf(ImageFormat.LARGE.maxBytes + 1)) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductImageStorageFailed::class.java)
    }

    @Test
    fun `the image of an unknown product should be refused`() {
        // Arrange
        val unknownProductImage = uploadedImageOf(1000, ProductId(Identity("unknown")))
        // Act
        val thrown = catchThrowable { productImageUseCases.add(unknownProductImage) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException::class.java)
    }

    @Test
    fun `a removed image should no longer be found`() {
        // Arrange
        productImageUseCases.add(uploadedImageOf(1000))
        // Act
        productImageUseCases.remove(deskLamp.productId)
        // Assert
        val thrown = catchThrowable { productImageUseCases.getImage(deskLamp.productId, ImageFormat.SMALL) }
        assertThat(thrown).isInstanceOf(ProductImageNotFoundException::class.java)
        assertThat(productImageUseCases.getProductsWithImage()).isEmpty()
    }
}
