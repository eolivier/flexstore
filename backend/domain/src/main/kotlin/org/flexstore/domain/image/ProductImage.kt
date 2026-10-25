package org.flexstore.domain.image

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

// The types of image that the shop accepts
enum class ImageType(val mediaType: String) {
    JPEG("image/jpeg"), PNG("image/png"), WEBP("image/webp");

    companion object {
        fun ofMediaType(mediaType: String) = entries.firstOrNull { it.mediaType == mediaType }
    }
}

// The bytes of an image (compared by their content)
class ImageBytes(val value: ByteArray) {

    fun size() = value.size

    override fun equals(other: Any?) = other is ImageBytes && value.contentEquals(other.value)

    override fun hashCode() = value.contentHashCode()
}

// An image: its bytes and their type
data class ImageContent(val bytes: ImageBytes, val type: ImageType) {
    fun size() = bytes.size()
}

// The formats in which an image of a product is kept: the large one for the page of the product,
// the small one for the cards of the catalog and the cart
enum class ImageFormat(val maxSide: Int, val maxBytes: Int) {
    LARGE(maxSide = 1600, maxBytes = 500 * 1024),
    SMALL(maxSide = 400, maxBytes = 60 * 1024);

    fun accepts(content: ImageContent) = content.size() <= maxBytes
}

// The image of a product in one format
data class ProductImage(val productId: ProductId, val format: ImageFormat, val content: ImageContent)

// An image given by an administrator for a product, before it is resized: 2 MB at most
data class UploadedImage(val productId: ProductId, val content: ImageContent) {

    fun isTooLarge() = content.size() > MAX_BYTES

    companion object {
        const val MAX_BYTES = 2 * 1024 * 1024
    }
}

// The bytes given are not an image of an accepted type
data class UnreadableImageException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ImageTooLargeException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ProductImageNotFoundException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class ProductImageStorageFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
