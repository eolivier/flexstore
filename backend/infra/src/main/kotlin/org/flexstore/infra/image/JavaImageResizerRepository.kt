package org.flexstore.infra.image

import org.flexstore.domain.image.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

private const val HIGHEST_QUALITY = 0.9f
private const val LOWEST_QUALITY = 0.4f
private const val QUALITY_STEP = 0.1f
private const val SHRINK_FACTOR = 0.8

// Resizes with ImageIO (JPEG, PNG; WebP through the TwelveMonkeys reader) and encodes as JPEG: the image is
// scaled down to the largest side of the format, then its quality is lowered, then it is scaled down again,
// until it fits the number of bytes of the format
class JavaImageResizerRepository : ImageResizerRepository {

    override fun resize(content: ImageContent, format: ImageFormat): ImageContent {
        val image = ImageIO.read(ByteArrayInputStream(content.bytes.value))
            ?: throw UnreadableImageException(ErrorMessage("The image cannot be read: JPEG, PNG or WebP expected."))
        var side = minOf(format.maxSide, maxOf(image.width, image.height))
        while (true) {
            val scaledImage = scaled(image, side)
            val jpeg = smallestJpegWithin(scaledImage, format.maxBytes)
            if (jpeg != null) return ImageContent(ImageBytes(jpeg), ImageType.JPEG)
            side = (side * SHRINK_FACTOR).toInt()
        }
    }

    // The image with its largest side at most at the given length,
    // on a white background (JPEG has no transparency)
    private fun scaled(image: BufferedImage, side: Int): BufferedImage {
        val ratio = minOf(1.0, side.toDouble() / maxOf(image.width, image.height))
        val width = maxOf(1, (image.width * ratio).toInt())
        val height = maxOf(1, (image.height * ratio).toInt())
        val scaledImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = scaledImage.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.drawImage(image, 0, 0, width, height, Color.WHITE, null)
        graphics.dispose()
        return scaledImage
    }

    // The JPEG of the best quality that fits the given number of bytes,
    // or null when even the lowest one does not
    private fun smallestJpegWithin(image: BufferedImage, maxBytes: Int): ByteArray? {
        var quality = HIGHEST_QUALITY
        while (quality >= LOWEST_QUALITY) {
            val jpeg = jpegOf(image, quality)
            if (jpeg.size <= maxBytes) return jpeg
            quality -= QUALITY_STEP
        }
        return null
    }

    private fun jpegOf(image: BufferedImage, quality: Float): ByteArray {
        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val parameters = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality
        }
        val output = ByteArrayOutputStream()
        ImageIO.createImageOutputStream(output).use { stream ->
            writer.output = stream
            writer.write(null, IIOImage(image, null, null), parameters)
        }
        writer.dispose()
        return output.toByteArray()
    }
}
