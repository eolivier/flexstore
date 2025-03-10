package org.flexstore.domain.entity

import org.flexstore.domain.Name
import org.flexstore.domain.NominalException
import org.flexstore.domain.NonEmptyString

data class User(val id: UserId, val name: Name, val email: Email)
data class UserId(val value: String)
data class Email(val value: String)

class Guest(name: Name) : Actor(name)

data class UserAlreadyExists(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserCreationFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
