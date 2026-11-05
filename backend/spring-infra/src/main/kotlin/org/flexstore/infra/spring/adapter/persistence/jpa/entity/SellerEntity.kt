package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// A version of the seller, never changed once saved: the current seller is the one with the highest version
@Entity
@Table(name = "seller")
data class SellerEntity(
    @Id
    val id: String,

    @Column(nullable = false, unique = true, updatable = false)
    val version: Int,

    @Column(nullable = false, updatable = false)
    val name: String,

    @Column(nullable = false, updatable = false)
    val address: String,

    @Column(nullable = false, updatable = false)
    val siret: String,

    @Column(name = "vat_number", nullable = false, updatable = false)
    val vatNumber: String,

    // How the customers reach the seller: null in the versions written before they were asked
    @Column(updatable = false)
    val phone: String?,

    @Column(updatable = false)
    val email: String?,

    @Column(updatable = false)
    val website: String?,

    // How the seller is incorporated: null in the versions written before it was asked
    @Column(name = "legal_form", updatable = false)
    val legalForm: String?,

    @Column(name = "share_capital", updatable = false)
    val shareCapital: String?,

    @Column(name = "registry_city", updatable = false)
    val registryCity: String?,

    // The accounts of the shop on the social networks: null when there is none, and in the versions written
    // before they were asked
    @Column(name = "facebook_url", updatable = false)
    val facebookUrl: String?,

    @Column(name = "instagram_url", updatable = false)
    val instagramUrl: String?,

    @Column(name = "x_url", updatable = false)
    val xUrl: String?,

    @Column(name = "tiktok_url", updatable = false)
    val tiktokUrl: String?,

    @Column(name = "youtube_url", updatable = false)
    val youtubeUrl: String?,

    @Column(name = "twitch_url", updatable = false)
    val twitchUrl: String?
) : TimestampedEntity()
