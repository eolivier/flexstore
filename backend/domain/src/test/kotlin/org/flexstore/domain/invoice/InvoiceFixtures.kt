package org.flexstore.domain.invoice

// The seller of the tests
val testSeller = Seller(
    SellerContact(SellerName("Flexstore SAS"), SellerAddress("1 rue de la Démo, 75001 Paris, France")),
    SellerIdentifiers(Siret("123 456 789 00012"), SellerVatNumber("FR12345678901"))
)
