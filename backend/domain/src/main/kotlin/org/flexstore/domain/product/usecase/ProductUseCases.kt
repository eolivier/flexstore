package org.flexstore.domain.product.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.ucop.domain.entity.Actor

// The use cases of the products of the catalog
class ProductUseCases(private val createProductUseCase: CreateProductUseCase,
                           private val readProductUseCase: ReadProductUseCase,
                           private val listProductsUseCase: ListProductsUseCase,
                           private val updateProductUseCase: UpdateProductUseCase,
                           private val proposeSkuUseCase: ProposeSkuUseCase) {

    // The use cases are shared by all the requests: the created product is read back with its id, not kept by
    // the use case
    fun createProduct(creator: Actor, draftProduct: DraftProduct): DefinedProduct {
        val productId = ProductId.nextId()
        createProductUseCase.unfold(CreateProductRequest(creator, productId, draftProduct))
        return readProductUseCase.unfold(ProductReading(creator, productId))
    }

    fun updateProduct(editor: Actor, product: DefinedProduct): DefinedProduct {
        updateProductUseCase.unfold(UpdateProductRequest(editor, product))
        return readProductUseCase.unfold(ProductReading(editor, product.productId))
    }

    fun getProducts(reader: Actor) = listProductsUseCase.unfold(reader)

    fun getProduct(reader: Actor, productId: ProductId) =
        readProductUseCase.unfold(ProductReading(reader, productId))

    fun proposeSkuFor(requester: Actor, skuBasis: SkuBasis) =
        proposeSkuUseCase.unfold(SkuProposalRequest(requester, skuBasis))
}
