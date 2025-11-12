package org.flexstore.domain.repository

import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId

interface UserRepository {
    fun exists(userId: UserId): Boolean
    fun notExists(userId: UserId): Boolean
    fun notExistsBasedOn(email: Email): Boolean
    fun save(user: User): User
    fun findById(id: UserId):User
    fun findAll(): List<User>
    fun delete(id: UserId):Boolean
}
