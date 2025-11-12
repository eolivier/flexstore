package org.flexstore.domain.usecase.product

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.Product
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*
import kotlin.reflect.KClass


class CreateProductUseCase(private val productRepository: ProductRepository) : UseCase<Product> {

    lateinit var createdProduct: Product

    override fun getPreConditions(): List<PreCondition<Product>> {
        println("#[BEGIN] CreateProductUseCase.getPreConditions")
        val preConditions = listOf(productDoesNotExistCondition())
        println("#[END] CreateProductUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<Product> {
        println("##[BEGIN] CreateProductUseCase.getNominalScenario")
        val createProductStep = Step<Product> { product -> createdProduct = productRepository.save(product) }
        val nominalScenario = NominalScenario(listOf(createProductStep))
        println("##[END] CreateProductUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<Product>> {
        println("#[BEGIN] CreateProductUseCase.getPostConditions")
        val postConditions = listOf(productExistsCondition())
        println("#[END] CreateProductUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii() : Map<KClass<out NominalException>, AlternativeScenario<Product>> {
        return emptyMap()
    }

    private fun productDoesNotExistCondition() = PreCondition<Product> { product ->
        if (productRepository.exists(product)) {
            throw ProductAlreadyExists(NonEmptyString("Product ${product} already exists."))
        }
    }

    private fun productExistsCondition() = PostCondition<Product> { product ->
        if (productRepository.notExists(product)) {
            throw ProductCreationFailed(NonEmptyString("Product ${product} was not created."))
        }
    }
}
