package org.flexstore.domain.order

import org.flexstore.domain.user.model.*

// The home of the customer of the tests, where the orders are delivered and billed
val home = PostalAddress(Street("12 rue des Lilas"), AddressComplement("Bâtiment B"), PostalCode("75011"), City("Paris"), Country("France"))

val homeAddresses = OrderAddresses.sameFor(home)
