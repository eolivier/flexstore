package org.flexstore.domain.order

import org.flexstore.domain.legal.LegalPageId
import org.flexstore.domain.order.model.AcceptedTermsOfSale

// The current terms of sale of the shop of the tests, accepted by its customers to place their orders
val currentTermsOfSaleId = LegalPageId("terms-of-sale-1")

val acceptedTermsOfSale = AcceptedTermsOfSale(currentTermsOfSaleId)
