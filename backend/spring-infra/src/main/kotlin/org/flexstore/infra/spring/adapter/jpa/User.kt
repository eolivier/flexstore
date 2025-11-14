package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*

@Entity
@Table(name = "\"user\"") // entre guillemets car "user" est mot réservé
data class UserEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String
)
