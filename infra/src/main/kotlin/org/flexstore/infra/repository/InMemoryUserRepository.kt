package org.flexstore.infra.repository

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository

class InMemoryUserRepository : UserRepository {

    private val users = mutableMapOf<UserId, User>()

    override fun exists(userId: UserId): Boolean = users.containsKey(userId)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun save(user: User) {
        users[user.id] = user
    }

    override fun findById(id: UserId): User = users[id] ?: throw IllegalArgumentException("User not found")

    override fun delete(id: UserId): Boolean = users.remove(id) != null
}