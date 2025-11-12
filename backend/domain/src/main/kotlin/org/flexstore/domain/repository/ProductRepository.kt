package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.Category
import org.flexstore.domain.valueobject.Product

interface ProductRepository {

    fun save(product: Product): Product
    fun findAll(): List<Product>
    fun findAllBy(category: Category): List<Product>
    fun exists(product: Product): Boolean
    fun notExists(product: Product): Boolean
}