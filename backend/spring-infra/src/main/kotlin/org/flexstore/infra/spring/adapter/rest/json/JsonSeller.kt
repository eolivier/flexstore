package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.seller.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// The seller written on the invoices: address on one line, SIRET of 14 digits (spaces allowed), intra-community
// VAT number (FR then 2 to 13 digits or letters)
data class JsonSeller @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("address") val address: String,
    @JsonProperty("siret") val siret: String,
    @JsonProperty("vatNumber") val vatNumber: String
) {
    constructor(seller: Seller) : this(
        seller.detail.contact.name.value,
        seller.detail.contact.address.value,
        seller.detail.identifier.siret.value,
        seller.detail.identifier.vatNumber.value
    )

    fun toSellerDetail(): SellerDetail {
        val contact = SellerContact(SellerName(name.trim()), SellerAddress(address.trim()))
        val identifier = SellerIdentifier(Siret(siret.trim()), SellerVatNumber(vatNumber.trim()))
        return SellerDetail(contact, identifier)
    }
}
