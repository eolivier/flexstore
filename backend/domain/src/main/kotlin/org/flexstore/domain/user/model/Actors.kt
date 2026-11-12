package org.flexstore.domain.user.model

import org.flexstore.domain.sharedkernel.Name
import org.ucop.domain.entity.Actor

// Who runs a use case, resolved before it runs. The roles are hierarchical: every Admin is a Customer, every
// Customer is a Visitor, so a precondition asks "is it at least a Customer?" with `actor is Customer`.
// Only the ActorResolutionService builds them (internal constructors): an Admin is one because the users say so.
open class Visitor internal constructor(name: Name = Name("Visitor")) : Actor(name)

open class Customer internal constructor(val userId: UserId, name: Name) : Visitor(name)

class Admin internal constructor(userId: UserId, name: Name) : Customer(userId, name)
