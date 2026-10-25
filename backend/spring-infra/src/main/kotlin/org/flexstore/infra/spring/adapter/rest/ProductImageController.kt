package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.image.*
import org.flexstore.domain.image.usecase.ProductImageUseCases
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.sharedkernel.Identity
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MultipartFile

// The images of the products: read by everyone, given by the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Product images")
@RestController
class ProductImageController(private val productImageUseCases: ProductImageUseCases) {

    @Operation(summary = "Retrieve the ids of the products that have an image")
    @GetMapping("/api/product-images")
    fun getProductsWithImage(): List<String> = productImageUseCases.getProductsWithImage().map { it.id.value }

    // The browser keeps the image and asks again with its ETag: an image that did not change is not sent again
    @Operation(summary = "Retrieve the image of a product, in the small (cards, cart) or large (page) format")
    @GetMapping("/api/products/{productId}/image")
    fun getImage(
        @PathVariable productId: String,
        @RequestParam(defaultValue = "large") format: String,
        request: WebRequest
    ): ResponseEntity<ByteArray> {
        val imageFormat = ImageFormat.valueOf(format.uppercase())
        val productImage = productImageUseCases.getImage(ProductId(Identity(productId)), imageFormat)
        val content = productImage.content
        val eTag = "\"${content.bytes.hashCode().toUInt().toString(16)}\""
        if (request.checkNotModified(eTag)) return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build()
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(content.type.mediaType))
            .cacheControl(CacheControl.noCache())
            .eTag(eTag)
            .body(content.bytes.value)
    }

    @Operation(summary = "Give the image of a product, 2 MB at most (JPEG, PNG or WebP): it is kept resized")
    @PutMapping("/api/admin/products/{productId}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addImage(@PathVariable productId: String, @RequestParam image: MultipartFile) {
        val imageType = ImageType.ofMediaType(image.contentType.orEmpty())
            ?: throw UnreadableImageException(ErrorMessage("The image must be a JPEG, a PNG or a WebP."))
        val content = ImageContent(ImageBytes(image.bytes), imageType)
        productImageUseCases.add(UploadedImage(ProductId(Identity(productId)), content))
    }

    @Operation(summary = "Remove the image of a product")
    @DeleteMapping("/api/admin/products/{productId}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeImage(@PathVariable productId: String) =
        productImageUseCases.remove(ProductId(Identity(productId)))
}
