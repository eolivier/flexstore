package org.flexstore.domain.user.model

// The users of the shop, customers and administrators
data class Users(private val users: List<User>) {

    constructor(vararg users: User) : this(users.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<User> = users.toList()
}
