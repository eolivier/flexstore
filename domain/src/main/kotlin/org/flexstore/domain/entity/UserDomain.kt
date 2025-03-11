package org.flexstore.domain.entity

import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.Actor
import org.ucop.domain.entity.Name

data class User(val id: UserId, val name: Name, val email: Email)
data class UserId(val value: String)
data class Email(val value: String)

class Guest(name: Name) : Actor(name)

data class UserAlreadyExists(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserCreationFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
