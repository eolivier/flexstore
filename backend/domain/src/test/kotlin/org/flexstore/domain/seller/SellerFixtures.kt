package org.flexstore.domain.seller


private val testSellerContact =
    SellerContact(SellerName("Flexstore SAS"), SellerAddress("1 rue de la Démo, 75001 Paris, France"))
private val testSellerIdentifier = SellerIdentifier(Siret("123 456 789 00012"), SellerVatNumber("FR12345678901"))

private val testSellerReach =
    SellerReach(
        SellerPhone("01 23 45 67 89"), SellerEmail("contact@flexstore.fr"), SellerWebsite("www.flexstore.fr")
    )

private val testSellerIncorporation =
    SellerIncorporation(SellerLegalForm("SAS"), ShareCapital("10 000"), RegistryCity("Paris"))

// What the tests write about the seller
val testSellerDetail =
    SellerDetail(testSellerContact, testSellerIdentifier, testSellerReach, testSellerIncorporation)

// The seller of the tests
val testSeller = Seller(SellerId("seller-1"), testSellerDetail)
