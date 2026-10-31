package org.flexstore.domain.invoice

data class SellerName(val value: String)

// The address of the seller, on one line
data class SellerAddress(val value: String)

// The SIRET of the seller (the French identifier of its establishment)
data class Siret(val value: String)

// The intra-community VAT number of the seller
data class SellerVatNumber(val value: String)

// Who the seller is and where it is
data class SellerContact(val name: SellerName, val address: SellerAddress)

// How the seller is registered
data class SellerIdentifiers(val siret: Siret, val vatNumber: SellerVatNumber)

// The shop that sells, as written on its invoices
data class Seller(val contact: SellerContact, val identifiers: SellerIdentifiers)
