package org.flexstore.domain.usecase.product

import org.flexstore.domain.valueobject.DefinedProduct
import org.flexstore.domain.valueobject.DraftProduct
import org.flexstore.domain.valueobject.ProductId

class ProductUseCaseFacade(private val createProductUseCase: CreateProductUseCase,
                           private val readProductUseCase: ReadProductUseCase) {

    // The use cases are shared by all the requests: the created product is read back with its id, not kept by the use case
    fun createProduct(draftProduct: DraftProduct): DefinedProduct {
        val productId = ProductId.nextId()
        createProductUseCase.unfold(CreateProductRequest(productId, draftProduct))
        return readProductUseCase.getProduct(productId)
    }

    fun getProducts() = readProductUseCase.getProducts()

    fun getProduct(productId: ProductId) = readProductUseCase.getProduct(productId)
}
