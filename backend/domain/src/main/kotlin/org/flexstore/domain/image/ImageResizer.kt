package org.flexstore.domain.image

// Makes an image fit a format: no side longer than its maximum, and no more bytes than its maximum.
// How (the encoding, the quality) is up to the infrastructure
interface ImageResizer {
    fun resize(content: ImageContent, format: ImageFormat): ImageContent
}
