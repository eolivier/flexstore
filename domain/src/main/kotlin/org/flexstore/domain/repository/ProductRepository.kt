package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.Category
import org.flexstore.domain.valueobject.Product

interface ProductRepository {

    fun findAll(): List<Product>
    fun findAllBy(category: Category): List<Product>
}