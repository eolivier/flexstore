package org.flexstore.domain.usecase.product

import org.flexstore.domain.repository.ProductRepository

class ReadProductUseCase(private val productRepository: ProductRepository) {
    fun getProducts() = productRepository.findAll()
}