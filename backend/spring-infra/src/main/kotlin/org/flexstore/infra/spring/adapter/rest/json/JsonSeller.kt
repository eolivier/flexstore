package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.seller.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// The seller written on the invoices: address on one line, SIRET of 14 digits (spaces allowed), intra-community
// VAT number (FR then 2 to 13 digits or letters), phone number, email and website (empty in the versions
// written before they were asked)
data class JsonSeller @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("address") val address: String,
    @JsonProperty("siret") val siret: String,
    @JsonProperty("vatNumber") val vatNumber: String,
    @JsonProperty("phone") val phone: String = "",
    @JsonProperty("email") val email: String = "",
    @JsonProperty("website") val website: String = ""
) {
    constructor(seller: Seller) : this(
        seller.detail.contact.name.value,
        seller.detail.contact.address.value,
        seller.detail.identifier.siret.value,
        seller.detail.identifier.vatNumber.value,
        seller.detail.reach.phone.value,
        seller.detail.reach.email.value,
        seller.detail.reach.website.value
    )

    fun toSellerDetail(): SellerDetail {
        val contact = SellerContact(SellerName(name.trim()), SellerAddress(address.trim()))
        val identifier = SellerIdentifier(Siret(siret.trim()), SellerVatNumber(vatNumber.trim()))
        val reach =
            SellerReach(SellerPhone(phone.trim()), SellerEmail(email.trim()), SellerWebsite(website.trim()))
        return SellerDetail(contact, identifier, reach)
    }
}
