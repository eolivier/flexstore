package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.Currency
import org.flexstore.domain.valueobject.Product
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/products")
class ProductController(private val productRepository: ProductRepository) {

    @GetMapping("/")
    fun getProducts(): List<JsonProduct> = productRepository.findAll().toJsonProducts()
}

private fun <E> List<E>.toJsonProducts(): List<JsonProduct> {
    return this.map {
        when (it) {
            is Product -> JsonProduct(
                it.productId.id.value,
                it.name.value,
                it.description?.value ?: "",
                it.price.amount.value,
                it.price.currency
            )
            else -> throw IllegalArgumentException("Unknown product type: ${it!!::class.java}")
        }
    }
}

data class JsonProduct @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: Currency
)
