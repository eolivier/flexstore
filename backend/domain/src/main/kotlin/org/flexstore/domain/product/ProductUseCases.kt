package org.flexstore.domain.product

// The use cases of the products of the catalog
class ProductUseCases(private val createProductUseCase: CreateProductUseCase,
                           private val readProductUseCase: ReadProductUseCase,
                           private val listProductsUseCase: ListProductsUseCase,
                           private val updateProductUseCase: UpdateProductUseCase,
                           private val proposeSkuUseCase: ProposeSkuUseCase) {

    // The use cases are shared by all the requests: the created product is read back with its id, not kept by
    // the use case
    fun createProduct(draftProduct: DraftProduct): DefinedProduct {
        val productId = ProductId.nextId()
        createProductUseCase.unfold(CreateProductRequest(productId, draftProduct))
        return readProductUseCase.unfold(productId)
    }

    fun updateProduct(product: DefinedProduct): DefinedProduct {
        updateProductUseCase.unfold(UpdateProductRequest(product))
        return readProductUseCase.unfold(product.productId)
    }

    fun getProducts() = listProductsUseCase.unfold(Unit)

    fun getProduct(productId: ProductId) = readProductUseCase.unfold(productId)

    fun proposeSkuFor(skuBasis: SkuBasis) = proposeSkuUseCase.unfold(skuBasis)
}
