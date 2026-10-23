package org.flexstore.domain.usecase.product

import org.flexstore.domain.valueobject.DraftProduct
import org.flexstore.domain.valueobject.Product
import org.flexstore.domain.valueobject.ProductId

class ProductUseCaseFacade(private val createProductUseCase: CreateProductUseCase,
                           private val readProductUseCase: ReadProductUseCase) {

    fun createProduct(draftProduct: DraftProduct): Product {
        createProductUseCase.unfold(draftProduct)
        return createProductUseCase.createdProduct
    }

    fun getProducts() = readProductUseCase.getProducts()

    fun getProduct(productId: ProductId) = readProductUseCase.getProduct(productId)
}