package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaProductImageRepository
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

// Product 7 of the catalog (Smartwatch): its image is given by the tests, then removed
private const val PRODUCT_ID = "7"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductImageIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var productImageRepository: JpaProductImageRepository

    @AfterEach
    fun removeImage() {
        productImageRepository.deleteAll(productImageRepository.findAll().filter { it.productId == PRODUCT_ID })
    }

    private fun png(width: Int, height: Int): ByteArray {
        val output = ByteArrayOutputStream()
        ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", output)
        return output.toByteArray()
    }

    private fun upload(bytes: ByteArray, mediaType: String = "image/png") = mockMvc.perform(
        multipart(HttpMethod.PUT, "/api/admin/products/$PRODUCT_ID/image")
            .file(MockMultipartFile("image", "watch.png", mediaType, bytes))
            .with(asAdmin())
    )

    @Test
    fun `an administrator should give the image of a product, served in both formats`() {
        // Act
        val response = upload(png(800, 600))
        // Assert
        response.andExpect(status().isNoContent)
        mockMvc.perform(get("/api/products/$PRODUCT_ID/image?format=small"))
            .andExpect(status().isOk).andExpect(content().contentType("image/jpeg"))
        mockMvc.perform(get("/api/products/$PRODUCT_ID/image?format=large"))
            .andExpect(status().isOk).andExpect(content().contentType("image/jpeg"))
        mockMvc.perform(get("/api/product-images")).andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasItem(PRODUCT_ID)))
    }

    @Test
    fun `an image that did not change should not be sent again`() {
        // Arrange
        upload(png(800, 600)).andExpect(status().isNoContent)
        val eTag = mockMvc.perform(get("/api/products/$PRODUCT_ID/image?format=small"))
            .andReturn().response.getHeader(HttpHeaders.ETAG)!!
        // Act
        val response = mockMvc.perform(get("/api/products/$PRODUCT_ID/image?format=small").header("If-None-Match", eTag))
        // Assert
        response.andExpect(status().isNotModified)
    }

    @Test
    fun `only an administrator should give the image of a product`() {
        // Act
        val response = mockMvc.perform(
            multipart(HttpMethod.PUT, "/api/admin/products/$PRODUCT_ID/image")
                .file(MockMultipartFile("image", "watch.png", "image/png", png(10, 10)))
                .with(asUser("it-image-customer"))
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `an image of more than 2 MB should be refused`() {
        // Act
        val response = upload(ByteArray(2 * 1024 * 1024 + 1))
        // Assert
        response.andExpect(status().isPayloadTooLarge)
    }

    @Test
    fun `a file that is not an image should be refused`() {
        // Act
        val response = upload("not an image".toByteArray(), mediaType = "text/plain")
        // Assert
        response.andExpect(status().isUnsupportedMediaType)
    }

    @Test
    fun `a removed image should no longer be served`() {
        // Arrange
        upload(png(800, 600)).andExpect(status().isNoContent)
        // Act
        mockMvc.perform(delete("/api/admin/products/$PRODUCT_ID/image").with(asAdmin())).andExpect(status().isNoContent)
        // Assert
        mockMvc.perform(get("/api/products/$PRODUCT_ID/image?format=small")).andExpect(status().isNotFound)
        mockMvc.perform(get("/api/products/$PRODUCT_ID/image")).andExpect(header().doesNotExist(HttpHeaders.ETAG))
    }
}
