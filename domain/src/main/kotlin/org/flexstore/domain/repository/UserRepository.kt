package org.flexstore.domain.repository

import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId

interface UserRepository {
    fun exists(userId: UserId): Boolean
    fun notExists(userId: UserId): Boolean
    fun save(user: User)
}
