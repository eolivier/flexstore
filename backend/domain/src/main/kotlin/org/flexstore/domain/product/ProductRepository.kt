package org.flexstore.domain.product

interface ProductRepository {

    fun save(product: Product): Product
    fun findAll(): List<Product>
    fun findById(productId: ProductId): DefinedProduct?
    fun findAllBy(category: Category): List<Product>
    fun exists(product: Product): Boolean
    fun notExists(product: Product): Boolean
    fun productExists(productId: ProductId): Boolean
    fun productIsMissing(productId: ProductId): Boolean = !productExists(productId)
}