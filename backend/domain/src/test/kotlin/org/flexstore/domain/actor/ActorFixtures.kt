package org.flexstore.domain.actor

import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.unknownUser
import org.flexstore.domain.user.administration.usersOfTheAdministration

// The actors of the tests of the use cases, resolved as in production by the ActorResolutionService, from the
// users of the administration tests: an Admin, a Customer, and a Visitor (a user that does not exist)
private val actorResolutionService = ActorResolutionService(usersOfTheAdministration())

val testAdministratorActor = actorResolutionService.actorOf(testAdministrator.id)

val testCustomerActor = actorResolutionService.actorOf(testCustomer.id)

val unknownUserActor = actorResolutionService.actorOf(unknownUser)
