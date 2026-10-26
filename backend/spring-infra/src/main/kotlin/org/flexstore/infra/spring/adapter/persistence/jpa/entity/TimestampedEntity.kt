package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

// The creation and last update dates of every stored row, set by Hibernate on insert and on update:
// the adapters never set them, and an entity rebuilt from the domain keeps the stored creation date
@MappedSuperclass
abstract class TimestampedEntity {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
}
