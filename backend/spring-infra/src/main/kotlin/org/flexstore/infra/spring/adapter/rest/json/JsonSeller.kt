package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.seller.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// The seller written on the invoices: address on one line, SIRET of 14 digits (spaces allowed), intra-community
// VAT number (FR then 2 to 13 digits or letters), phone number, email and website, legal form, share capital in
// euros and city of the trade register (empty in the versions written before they were asked), and the accounts
// of the shop on the social networks
data class JsonSeller @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("address") val address: String,
    @JsonProperty("siret") val siret: String,
    @JsonProperty("vatNumber") val vatNumber: String,
    @JsonProperty("phone") val phone: String = "",
    @JsonProperty("email") val email: String = "",
    @JsonProperty("website") val website: String = "",
    @JsonProperty("legalForm") val legalForm: String = "",
    @JsonProperty("shareCapital") val shareCapital: String = "",
    @JsonProperty("registryCity") val registryCity: String = "",
    // Optional when written (the clients written before the social networks were asked do not send it): the
    // nullable type stays at this JSON boundary, and the seller written has none
    @JsonProperty("socialNetworks") val socialNetworks: JsonSocialNetworks? = null
) {
    constructor(seller: Seller) : this(
        seller.detail.contact.name.value,
        seller.detail.contact.address.value,
        seller.detail.identifier.siret.value,
        seller.detail.identifier.vatNumber.value,
        seller.detail.reach.phone.value,
        seller.detail.reach.email.value,
        seller.detail.reach.website.value,
        seller.detail.incorporation.legalForm.value,
        seller.detail.incorporation.shareCapital.value,
        seller.detail.incorporation.registryCity.value,
        JsonSocialNetworks(seller.detail.socialNetworks)
    )

    fun toSellerDetail(): SellerDetail {
        val contact = SellerContact(SellerName(name.trim()), SellerAddress(address.trim()))
        val identifier = SellerIdentifier(Siret(siret.trim()), SellerVatNumber(vatNumber.trim()))
        val reach =
            SellerReach(SellerPhone(phone.trim()), SellerEmail(email.trim()), SellerWebsite(website.trim()))
        val incorporation = SellerIncorporation(
            SellerLegalForm(legalForm.trim()),
            ShareCapital(shareCapital.trim()),
            RegistryCity(registryCity.trim())
        )
        val givenSocialNetworks = socialNetworks?.toSocialNetworks() ?: SocialNetworks.none()
        return SellerDetail(contact, identifier, reach, incorporation, givenSocialNetworks)
    }
}
