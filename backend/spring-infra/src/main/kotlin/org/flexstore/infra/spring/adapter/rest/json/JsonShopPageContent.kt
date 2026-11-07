package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.pagecontent.*

// The contents of the shop pages as everyone reads them: one for each type, empty until the administrators
// write it
data class JsonShopPageContents(val contents: List<JsonShopPageContent>) {
    constructor(shopPageContents: ShopPageContents) :
        this(ShopPageContentType.entries.map { JsonShopPageContent(shopPageContents.of(it)) })
}

data class JsonShopPageContent(val type: ShopPageContentType, val text: String) {
    constructor(content: ShopPageContent) : this(content.type, content.text.value)
}

// The new text of a content, written by an administrator
data class JsonShopPageContentText @JsonCreator constructor(@JsonProperty("text") val text: String) {

    fun toContent(type: ShopPageContentType) = ShopPageContent(type, ShopPageContentText(text.trim()))
}
