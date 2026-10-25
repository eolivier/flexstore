package org.flexstore.domain.order

import org.flexstore.domain.user.model.*

// The customer of the tests, who receives the orders
val janeDoe = PrivateIndividual(FullName(FirstName("Jane"), LastName("Doe")))

private val homeStreet = Street("12 rue des Lilas")
private val homeComplement = AddressComplement("Bâtiment B")

// The home of the customer of the tests, where the orders are delivered and billed
val homeTown = Town(PostalCode("75011"), City("Paris"))
val homeLocation = Location(StreetAddress(homeStreet, homeComplement), Locality(homeTown, Country("France")))

val homeAddress = PostalAddress(janeDoe, homeLocation)

private val officeStreet = Street("3 place du Marché")

private val officeTown = Town(PostalCode("69001"), City("Lyon"))
private val officeLocation = Location(StreetAddress(officeStreet), Locality(officeTown, Country("France")))

val officeAddress = PostalAddress(janeDoe, officeLocation)

val homeOrderAddresses = OrderAddresses.sameFor(homeAddress)
