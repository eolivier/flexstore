package org.flexstore.infra.repository

import org.flexstore.domain.pagecontent.*

// The contents of the shop pages in memory: for the tests
class InMemoryShopPageContentRepository(private var contents: ShopPageContents = ShopPageContents()) :
    ShopPageContentRepository {

    override fun all() = contents

    override fun save(content: ShopPageContent) {
        contents = contents.with(content)
    }
}
