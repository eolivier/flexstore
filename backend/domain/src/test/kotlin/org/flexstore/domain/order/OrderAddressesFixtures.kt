package org.flexstore.domain.order

import org.flexstore.domain.user.model.*

// The homeAddress of the customer of the tests, where the orders are delivered and billed
val homeAddress = PostalAddress(Street("12 rue des Lilas"), AddressComplement("Bâtiment B"), PostalCode("75011"), City("Paris"), Country("France"))

val homeOrderAddresses = OrderAddresses.sameFor(homeAddress)
