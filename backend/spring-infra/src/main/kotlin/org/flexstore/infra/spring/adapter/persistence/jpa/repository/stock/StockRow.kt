package org.flexstore.infra.spring.adapter.persistence.jpa.repository.stock

import org.flexstore.domain.stock.*

// The stock of a product as stored: its units are empty while the stock was never given
data class StockRow(val productId: String, val quantity: Int?, val threshold: Int)
