package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The content of one type of the shop pages: its row is created by the migrations, only its text changes
@Entity
@Table(name = "shop_page_content")
data class ShopPageContentEntity(
    @Id
    val id: String,

    // META_KEY_WORDS, GOOGLE_ANALYTICS_ID or META_SHOP_DESCRIPTION
    @Column(name = "page_type", nullable = false, updatable = false)
    val pageType: String,

    @Column(nullable = false)
    val content: String
) : TimestampedEntity()
