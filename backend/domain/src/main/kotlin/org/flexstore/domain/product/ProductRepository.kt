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
    // Another product of the catalog, with another id, has the SKU of this one
    fun skuIsUsedByAnotherProduct(product: DefinedProduct): Boolean
    // No product of the catalog has this SKU
    fun skuIsFree(sku: Sku): Boolean
    fun skuIsNotFree(sku: Sku): Boolean = !skuIsFree(sku)
}