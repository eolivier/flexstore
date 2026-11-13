package org.flexstore.infra.spring.adapter.persistence.jpa.repository.payment

import org.flexstore.domain.order.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.PaymentEntity
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaPaymentRepository : JpaRepository<PaymentEntity, String>
