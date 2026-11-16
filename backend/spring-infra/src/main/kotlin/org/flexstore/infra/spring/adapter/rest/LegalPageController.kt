package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.legal.LegalPageKind
import org.flexstore.domain.legal.usecase.LegalPageUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonLegalPage
import org.flexstore.infra.spring.adapter.rest.json.JsonLegalText
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// How each legal page is named in the paths of the API
private val legalPageKinds = mapOf(
    "legal-notice" to LegalPageKind.LEGAL_NOTICE,
    "terms-of-sale" to LegalPageKind.TERMS_OF_SALE,
    "privacy-policy" to LegalPageKind.PRIVACY_POLICY
)

// The legal pages of the shop, read by everyone
@RestController
@RequestMapping("/api/legal-pages")
class LegalPageController(
    private val legalPageUseCases: LegalPageUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping("/{kind}")
    fun getLegalPage(@PathVariable kind: String): ResponseEntity<JsonLegalPage> {
        val legalPageKind = legalPageKinds[kind] ?: return ResponseEntity.notFound().build()
        val legalPage = legalPageUseCases.legalPage(actorResolutionService.visitor(), legalPageKind)
        return ResponseEntity.ok(JsonLegalPage(legalPage))
    }
}

// The legal pages written by the administrators (ADMIN role, see SecurityConfig)
@RestController
@RequestMapping("/api/admin/legal-pages")
class AdminLegalPageController(
    private val legalPageUseCases: LegalPageUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @PutMapping("/{kind}")
    fun publishLegalPage(
        @AuthenticationPrincipal publisher: Jwt,
        @PathVariable kind: String,
        @RequestBody legalText: JsonLegalText
    ): ResponseEntity<JsonLegalPage> {
        val legalPageKind = legalPageKinds[kind] ?: return ResponseEntity.notFound().build()
        val actor = actorResolutionService.actorOf(publisher)
        val legalPage = legalPageUseCases.publish(actor, legalPageKind, legalText.toLegalText())
        return ResponseEntity.ok(JsonLegalPage(legalPage))
    }
}
