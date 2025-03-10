package org.flexstore.domain.entity

import org.flexstore.domain.Name

data class User(val id: UserId, val name: Name, val email: Email)
data class UserId(val value: String)
data class Email(val value: String)

class Guest(name: Name) : Actor(name)
