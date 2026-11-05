package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.seller.SocialNetwork
import org.flexstore.domain.seller.SocialNetworkUrl
import org.flexstore.domain.seller.SocialNetworks

// The accounts of the shop on the social networks, as web addresses with or without their scheme; empty when the
// shop has no account on a network. Each one is optional when it is written: a missing one stays empty
data class JsonSocialNetworks(
    var facebook: String = "",
    var instagram: String = "",
    var x: String = "",
    var tiktok: String = "",
    var youtube: String = "",
    var twitch: String = ""
) {
    constructor(socialNetworks: SocialNetworks) : this(
        socialNetworks.urlOf(SocialNetwork.FACEBOOK).value,
        socialNetworks.urlOf(SocialNetwork.INSTAGRAM).value,
        socialNetworks.urlOf(SocialNetwork.X).value,
        socialNetworks.urlOf(SocialNetwork.TIKTOK).value,
        socialNetworks.urlOf(SocialNetwork.YOUTUBE).value,
        socialNetworks.urlOf(SocialNetwork.TWITCH).value
    )

    fun toSocialNetworks() = SocialNetworks.of(
        mapOf(
            SocialNetwork.FACEBOOK to SocialNetworkUrl(facebook.trim()),
            SocialNetwork.INSTAGRAM to SocialNetworkUrl(instagram.trim()),
            SocialNetwork.X to SocialNetworkUrl(x.trim()),
            SocialNetwork.TIKTOK to SocialNetworkUrl(tiktok.trim()),
            SocialNetwork.YOUTUBE to SocialNetworkUrl(youtube.trim()),
            SocialNetwork.TWITCH to SocialNetworkUrl(twitch.trim())
        )
    )
}
