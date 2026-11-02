package org.flexstore.domain.seller

import org.flexstore.domain.sharedkernel.Identity

private val testSellerContact =
    SellerContact(SellerName("Flexstore SAS"), SellerAddress("1 rue de la Démo, 75001 Paris, France"))
private val testSellerIdentifier = SellerIdentifier(Siret("123 456 789 00012"), SellerVatNumber("FR12345678901"))

private val testSellerReach =
    SellerReach(SellerPhone("01 23 45 67 89"), SellerEmail("contact@flexstore.fr"), SellerWebsite("www.flexstore.fr"))

// What the tests write about the seller
val testSellerDetail = SellerDetail(testSellerContact, testSellerIdentifier, testSellerReach)

// The seller of the tests
val testSeller = Seller(SellerId(Identity("seller-1")), testSellerDetail)
