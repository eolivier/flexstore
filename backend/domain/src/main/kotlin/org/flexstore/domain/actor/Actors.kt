package org.flexstore.domain.actor

import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.entity.Actor

// Who runs a use case, resolved before it runs. The roles are hierarchical: every Admin is a Customer, every
// Customer is a Visitor, so a precondition asks "is it at least a Customer?" with `isAtLeastCustomer()`.
// Only the ActorResolutionService builds them (internal constructors): an Admin is one because the users say so.
open class Visitor internal constructor(name: Name = Name("Visitor")) : Actor(name)

open class Customer internal constructor(val userId: UserId, name: Name) : Visitor(name)

class Admin internal constructor(userId: UserId, name: Name) : Customer(userId, name)

// What a precondition asks of its actor. The roles are hierarchical: "is it exactly this role?" or "is it at
// least this role?". Every resolved actor is at least a Visitor, so there is no question for that role. The
// SystemActor is none of them.
fun Actor.isAdmin() = this is Admin

fun Actor.isNotAdmin() = !isAdmin()

fun Actor.isAtLeastCustomer() = this is Customer

fun Actor.isCustomer() = isAtLeastCustomer() && isNotAdmin()

fun Actor.isVisitor() = this is Visitor && !isAtLeastCustomer()

// The system itself, which runs the scheduled use cases. It is not a user: the domain services that the
// schedulers call instantiate it (internal constructor), so that no request and no user id can resolve to it.
class SystemActor internal constructor() : Actor(Name("System"))

// Only an Admin and the system run a use case that a scheduler triggers
fun Actor.isSystem() = this is SystemActor

fun Actor.isNotSystem() = !isSystem()

fun Actor.cannotRunScheduledUseCases() = isNotAdmin() && isNotSystem()
