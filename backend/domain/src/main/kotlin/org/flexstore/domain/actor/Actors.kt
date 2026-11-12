package org.flexstore.domain.actor

import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.entity.Actor

// Who runs a use case, resolved before it runs. The roles are hierarchical: every Admin is a Customer, every
// Customer is a Visitor, so a precondition asks "is it at least a Customer?" with `actor is Customer`.
// Only the ActorResolutionService builds them (internal constructors): an Admin is one because the users say so.
open class Visitor internal constructor(name: Name = Name("Visitor")) : Actor(name)

open class Customer internal constructor(val userId: UserId, name: Name) : Visitor(name)

class Admin internal constructor(userId: UserId, name: Name) : Customer(userId, name)

fun Actor.isNotAdmin() = this !is Admin

// The system itself, which runs the scheduled use cases. It is not a user: the domain services that the schedulers
// call instantiate it (internal constructor), so that no request and no user id can ever resolve to it.
class SystemActor internal constructor() : Actor(Name("System"))

// Only an Admin and the system run a use case that a scheduler triggers
fun Actor.cannotRunScheduledUseCases() = this !is Admin && this !is SystemActor
