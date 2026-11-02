package org.flexstore.domain.order

import org.flexstore.domain.legal.LegalPageId
import org.flexstore.domain.sharedkernel.Identity

// The current terms of sale of the shop of the tests, accepted by its customers to place their orders
val currentTermsOfSaleId = LegalPageId(Identity("terms-of-sale-1"))

val acceptedTermsOfSale = AcceptedTermsOfSale(currentTermsOfSaleId)
