package org.flexstore.domain.order

import org.flexstore.domain.user.model.*

// The customer of the tests, who receives the orders
val janeDoe = Recipient(FullName(FirstName("Jane"), LastName("Doe")))

private val homeStreet = Street("12 rue des Lilas")
private val homeComplement = AddressComplement("Bâtiment B")

// The home of the customer of the tests, where the orders are delivered and billed
val homeLocation = Location(homeStreet, homeComplement, PostalCode("75011"), City("Paris"), Country("France"))

val homeAddress = PostalAddress(janeDoe, homeLocation)

private val officeStreet = Street("3 place du Marché")

private val officeLocation = Location(officeStreet, null, PostalCode("69001"), City("Lyon"), Country("France"))

val officeAddress = PostalAddress(janeDoe, officeLocation)

val homeOrderAddresses = OrderAddresses.sameFor(homeAddress)
