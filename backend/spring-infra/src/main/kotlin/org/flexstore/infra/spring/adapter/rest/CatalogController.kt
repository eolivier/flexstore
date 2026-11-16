package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.*
import org.flexstore.domain.product.usecase.ProductUseCases
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.mapping.languageAcceptedIn
import org.flexstore.infra.spring.adapter.mapping.toJsonProducts
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// The catalog a page at a time, for the customers who search it, filter it by category and sort it
@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val productUseCases: ProductUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getCatalogPage(
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") acceptLanguage: String,
        @RequestParam(defaultValue = "${PageNumber.FIRST}") page: Int,
        @RequestParam(defaultValue = "${PageSize.DEFAULT}") size: Int,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "") category: String,
        @RequestParam(defaultValue = "") sort: String
    ): ResponseEntity<List<JsonProduct>> {
        val language = languageAcceptedIn(acceptLanguage)
        val productFilter = ProductFilter(ProductSearchText(search), CategoryFilter.of(category))
        val catalogPaging = CatalogPaging(ProductSort.of(sort), PageRequest.of(page, size))
        val catalogSearch = CatalogSearch(language, productFilter, catalogPaging)
        val catalogPage = productUseCases.searchCatalog(actorResolutionService.visitor(), catalogSearch)
        return pagedBody(catalogPage.content.asList().toJsonProducts(language), catalogPage.total)
    }
}
