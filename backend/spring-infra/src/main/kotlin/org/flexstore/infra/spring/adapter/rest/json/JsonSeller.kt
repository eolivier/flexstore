package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.invoice.*

// The seller written on the invoices: address on one line, SIRET of 14 digits (spaces allowed), intra-community
// VAT number (FR then 2 to 13 digits or letters)
data class JsonSeller @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("address") val address: String,
    @JsonProperty("siret") val siret: String,
    @JsonProperty("vatNumber") val vatNumber: String
) {
    constructor(seller: Seller) : this(
        seller.details.contact.name.value,
        seller.details.contact.address.value,
        seller.details.identifiers.siret.value,
        seller.details.identifiers.vatNumber.value
    )

    fun toSellerDetails(): SellerDetails {
        val contact = SellerContact(SellerName(name.trim()), SellerAddress(address.trim()))
        val identifiers = SellerIdentifiers(Siret(siret.trim()), SellerVatNumber(vatNumber.trim()))
        return SellerDetails(contact, identifiers)
    }
}
