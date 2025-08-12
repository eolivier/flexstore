package org.flexstore.domain.usecase.user.withoutid

import org.flexstore.domain.entity.User
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class CreateUserWithoutIdUseCase(private val userRepository: UserRepository) : UseCase<User> {

    lateinit var createdUser: User

    override fun getPreConditions(): List<PreCondition<User>> {
       TODO("getPreConditions() not implemented")
    }

    override fun getNominalScenario(): NominalScenario<User> {
        TODO("getNominalScenario() not implemented")
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        TODO("getPostConditions() not implemented")
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<User>> {
        return emptyMap()
    }
}