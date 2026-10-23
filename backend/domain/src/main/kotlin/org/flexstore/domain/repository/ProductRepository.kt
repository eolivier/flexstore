package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.Category
import org.flexstore.domain.valueobject.DefinedProduct
import org.flexstore.domain.valueobject.Product
import org.flexstore.domain.valueobject.ProductId

interface ProductRepository {

    fun save(product: Product): Product
    fun findAll(): List<Product>
    fun findById(productId: ProductId): DefinedProduct?
    fun findAllBy(category: Category): List<Product>
    fun exists(product: Product): Boolean
    fun notExists(product: Product): Boolean
}