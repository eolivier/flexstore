package org.flexstore.domain.actor

import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.Actor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// Who runs a use case, resolved before it runs. The roles are hierarchical: every Admin is a Customer, every
// Customer is a Visitor, so a precondition asks "is it at least a Customer?" with `isAtLeastCustomer()`.
// Only the ActorResolutionService builds them (internal constructors): an Admin is one because the users say so.
open class Visitor internal constructor(name: Name = Name("Visitor")) : Actor(name)

open class Customer internal constructor(
    val userId: UserId.ValidUserId,
    name: Name,
    val status: AccountStatus
) : Visitor(name) {

    // A customer (or an admin) whose account is neither suspended, locked nor disabled
    fun canAct() = status.allowsSignIn()

    fun cannotAct() = !canAct()
}

class Admin internal constructor(userId: UserId.ValidUserId, name: Name, status: AccountStatus) :
    Customer(userId, name, status)

// What a precondition asks of its actor. The roles are hierarchical: "is it exactly this role?" or "is it at
// least this role?". Every resolved actor is at least a Visitor, so there is no question for that role. The
// SystemActor is none of them.
fun Actor.isAdmin() = this is Admin

fun Actor.isNotAdmin() = !isAdmin()

// A customer or an admin: the actor is then known as a Customer, which tells who it is and whether it can act
@OptIn(ExperimentalContracts::class)
fun Actor.isAtLeastCustomer(): Boolean {
    contract { returns(true) implies (this@isAtLeastCustomer is Customer) }
    return this is Customer
}

fun Actor.isNotAtLeastCustomer() = !isAtLeastCustomer()

fun Actor.isCustomer() = isAtLeastCustomer() && isNotAdmin()

fun Actor.isVisitor() = this is Visitor && !isAtLeastCustomer()

// A visitor, or higher: a customer or an admin is one too, and so is the system, which stands above every role.
// What the preconditions of the public reads ask of their reader
fun Actor.isAtLeastVisitor() = this is Visitor || isSystem()

fun Actor.isNotAtLeastVisitor() = !isAtLeastVisitor()

// The user behind a customer or an admin, for a use case whose precondition checked that its actor is one
fun Actor.customerId(): UserId.ValidUserId =
    if (isAtLeastCustomer()) userId else throw IllegalStateException("The actor is not at least a customer.")

// What the preconditions of the use cases of a customer check first: the actor is a customer or an admin who can
// act. A visitor is a user that does not exist; a customer whose account is suspended, locked or disabled is
// refused
fun Actor.verifyCanAct() {
    if (isNotAtLeastCustomer()) {
        throw UserNotFoundException(ErrorMessage("User not found."))
    }
    if (isAtLeastCustomer() && cannotAct()) {
        val message = ErrorMessage("The account ${userId.value} is $status: it cannot act.")
        throw AccountRefusedException(status, message)
    }
}

// Whether the actor is the account of this user, to tell a user from the others they act on
fun Actor.isTheUser(userId: UserId) = this is Customer && this.userId == userId

// The system itself, which runs the scheduled use cases. It is not a user: the domain services that the
// schedulers call instantiate it (internal constructor), so that no request and no user id can resolve to it.
class SystemActor internal constructor() : Actor(Name("System"))

// Only an Admin and the system run a use case that a scheduler triggers
fun Actor.isSystem() = this is SystemActor

fun Actor.isNotSystem() = !isSystem()

fun Actor.cannotRunScheduledUseCases() = isNotAdmin() && isNotSystem()
