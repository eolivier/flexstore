package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.legal.LegalPageKind
import org.flexstore.domain.legal.usecase.LegalPageUseCases
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonLegalPage
import org.flexstore.infra.spring.adapter.rest.json.JsonLegalText
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// How each legal page is named in the paths of the API
private val legalPageKinds = mapOf(
    "legal-notice" to LegalPageKind.LEGAL_NOTICE,
    "terms-of-sale" to LegalPageKind.TERMS_OF_SALE
)

// The legal pages of the shop, read by everyone
@Tag(name = "Legal pages")
@RestController
@RequestMapping("/api/legal-pages")
class LegalPageController(private val legalPageUseCases: LegalPageUseCases) {

    @Operation(summary = "Retrieve the current version of a legal page: legal-notice or terms-of-sale")
    @GetMapping("/{kind}")
    fun getLegalPage(@PathVariable kind: String): ResponseEntity<JsonLegalPage> {
        val legalPageKind = legalPageKinds[kind] ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(JsonLegalPage(legalPageUseCases.legalPage(legalPageKind)))
    }
}

// The legal pages written by the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/legal-pages")
class AdminLegalPageController(private val legalPageUseCases: LegalPageUseCases) {

    @Operation(summary = "Publish a new version of a legal page: legal-notice or terms-of-sale")
    @PutMapping("/{kind}")
    fun publishLegalPage(
        @PathVariable kind: String,
        @RequestBody legalText: JsonLegalText
    ): ResponseEntity<JsonLegalPage> {
        val legalPageKind = legalPageKinds[kind] ?: return ResponseEntity.notFound().build()
        val legalPage = legalPageUseCases.publish(legalPageKind, legalText.toLegalText())
        return ResponseEntity.ok(JsonLegalPage(legalPage))
    }
}
