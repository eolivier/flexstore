package org.flexstore.domain.image

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException
import org.flexstore.domain.user.model.UserId

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

private const val BYTES_PER_KILOBYTE = 1024

// The formats in which an image of a product is kept: the large one for the page of the product,
// the small one for the cards of the catalog and the cart
enum class ImageFormat(val maxSide: Int, val maxBytes: Int) {
    LARGE(maxSide = 1600, maxBytes = 500 * BYTES_PER_KILOBYTE),
    SMALL(maxSide = 400, maxBytes = 60 * BYTES_PER_KILOBYTE);

    fun accepts(content: ImageContent) = content.size() <= maxBytes
}

// The image of a product in one format
data class ProductImage(val productId: ProductId, val format: ImageFormat, val content: ImageContent)

// An image given by an administrator for a product, before it is resized: 2 MB at most
data class UploadedImage(val productId: ProductId, val content: ImageContent) {

    fun isTooLarge() = content.size() > MAX_BYTES

    companion object {
        const val MAX_MEGABYTES = 2
        const val MAX_BYTES = MAX_MEGABYTES * BYTES_PER_KILOBYTE * BYTES_PER_KILOBYTE
    }
}

// The bytes given are not an image of an accepted type
// An administrator who gives the image of a product
data class ProductImageUpload(val editor: UserId, val uploadedImage: UploadedImage)

// An administrator who removes the image of a product
data class ProductImageRemoval(val editor: UserId, val productId: ProductId)

data class ProductImageForbiddenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class UnreadableImageException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ImageTooLargeException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ProductImageNotFoundException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class ProductImageStorageFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
