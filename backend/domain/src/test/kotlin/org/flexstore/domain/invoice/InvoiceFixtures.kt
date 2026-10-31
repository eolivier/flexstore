package org.flexstore.domain.invoice

import org.flexstore.domain.sharedkernel.Identity

private val testSellerContact =
    SellerContact(SellerName("Flexstore SAS"), SellerAddress("1 rue de la Démo, 75001 Paris, France"))
private val testSellerIdentifiers = SellerIdentifiers(Siret("123 456 789 00012"), SellerVatNumber("FR12345678901"))

// What the tests write about the seller
val testSellerDetails = SellerDetails(testSellerContact, testSellerIdentifiers)

// The seller of the tests
val testSeller = Seller(SellerId(Identity("seller-1")), testSellerDetails)
