package org.flexstore.domain.pagecontent

// The contents of the shop pages: one for each type, created empty by the migrations
interface ShopPageContentRepository {

    fun all(): ShopPageContents

    // Replaces the content of its type
    fun save(content: ShopPageContent)
}
