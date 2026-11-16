package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.pagecontent.ShopPageContentType
import org.flexstore.domain.pagecontent.usecase.ShopPageContentUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonShopPageContentText
import org.flexstore.infra.spring.adapter.rest.json.JsonShopPageContents
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// How each type of content is named in the paths of the API
private val shopPageContentTypes = mapOf(
    "meta-key-words" to ShopPageContentType.META_KEY_WORDS,
    "google-analytics-id" to ShopPageContentType.GOOGLE_ANALYTICS_ID,
    "meta-shop-description" to ShopPageContentType.META_SHOP_DESCRIPTION
)

// The contents of the shop pages, read by everyone: the head of the pages shows them
@RestController
@RequestMapping("/api/shop-page-contents")
class ShopPageContentController(
    private val shopPageContentUseCases: ShopPageContentUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getContents() = JsonShopPageContents(shopPageContentUseCases.contents(actorResolutionService.visitor()))
}

// The contents of the shop pages edited by the administrators (ADMIN role, see SecurityConfig, checked again by
// the use case)
@RestController
@RequestMapping("/api/admin/shop-page-contents")
class AdminShopPageContentController(
    private val shopPageContentUseCases: ShopPageContentUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @PutMapping("/{type}")
    fun editContent(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable type: String,
        @RequestBody contentText: JsonShopPageContentText
    ): ResponseEntity<JsonShopPageContents> {
        val contentType = shopPageContentTypes[type] ?: return ResponseEntity.notFound().build()
        val actor = actorResolutionService.actorOf(editor)
        val contents = shopPageContentUseCases.edit(actor, contentText.toContent(contentType))
        return ResponseEntity.ok(JsonShopPageContents(contents))
    }
}
