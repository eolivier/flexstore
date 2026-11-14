package org.flexstore.domain.product

import org.flexstore.domain.product.model.Category
import org.flexstore.domain.product.model.DefinedProduct
import org.flexstore.domain.product.model.Product
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.product.model.Sku
import org.flexstore.domain.product.model.ProductNotFoundException
import org.ucop.domain.ErrorMessage

interface ProductRepository {

    fun save(product: Product): Product
    fun findAll(): List<Product>
    fun findById(productId: ProductId): DefinedProduct?
    // The product, which a precondition has checked to exist
    fun get(productId: ProductId): DefinedProduct =
        findById(productId) ?: throw ProductNotFoundException(ErrorMessage("Product $productId not found."))
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