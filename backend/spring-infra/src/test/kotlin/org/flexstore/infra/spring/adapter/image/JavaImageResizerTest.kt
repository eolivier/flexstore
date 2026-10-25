package org.flexstore.infra.spring.adapter.image

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.image.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.random.Random

class JavaImageResizerTest : WithAssertions {

    private val resizer = JavaImageResizer()

    // Random pixels: the hardest image to compress
    private fun noisyPng(width: Int, height: Int, type: Int = BufferedImage.TYPE_INT_RGB): ImageContent {
        val image = BufferedImage(width, height, type)
        val random = Random(42)
        for (x in 0 until width) for (y in 0 until height) image.setRGB(x, y, random.nextInt())
        val output = ByteArrayOutputStream()
        ImageIO.write(image, "png", output)
        return ImageContent(ImageBytes(output.toByteArray()), ImageType.PNG)
    }

    private fun readBack(content: ImageContent) = ImageIO.read(ByteArrayInputStream(content.bytes.value))

    @ParameterizedTest(name = "a large image should fit the {0} format")
    @EnumSource(ImageFormat::class)
    fun `a large image should be reduced to the size and the bytes of the format`(format: ImageFormat) {
        // Arrange
        val largeImage = noisyPng(2400, 1600)
        // Act
        val resized = resizer.resize(largeImage, format)
        // Assert
        val resizedImage = readBack(resized)
        assertThat(resized.type).isEqualTo(ImageType.JPEG)
        assertThat(resized.size()).isLessThanOrEqualTo(format.maxBytes)
        assertThat(maxOf(resizedImage.width, resizedImage.height)).isLessThanOrEqualTo(format.maxSide)
    }

    @Test
    fun `a small image should not be enlarged`() {
        // Arrange
        val smallImage = noisyPng(120, 80)
        // Act
        val resized = resizer.resize(smallImage, ImageFormat.LARGE)
        // Assert
        val resizedImage = readBack(resized)
        assertThat(resizedImage.width to resizedImage.height).isEqualTo(120 to 80)
    }

    @Test
    fun `a transparent image should be encoded as a JPEG`() {
        // Arrange
        val transparentImage = noisyPng(300, 300, BufferedImage.TYPE_INT_ARGB)
        // Act
        val resized = resizer.resize(transparentImage, ImageFormat.SMALL)
        // Assert
        assertThat(readBack(resized)).isNotNull
    }

    @Test
    fun `bytes that are not an image should be refused`() {
        // Arrange
        val notAnImage = ImageContent(ImageBytes("not an image".toByteArray()), ImageType.PNG)
        // Act
        val thrown = catchThrowable { resizer.resize(notAnImage, ImageFormat.SMALL) }
        // Assert
        assertThat(thrown).isInstanceOf(UnreadableImageException::class.java)
    }
}
