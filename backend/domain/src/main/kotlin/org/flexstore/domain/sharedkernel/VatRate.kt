package org.flexstore.domain.sharedkernel

import java.math.BigDecimal

// The rates of VAT in France, in percent: each product is sold at one of them, and its price includes it
enum class VatRate(val percent: BigDecimal) {
    // Most goods and services
    STANDARD(BigDecimal("20")),

    // Restaurants, passenger transport, renovation works
    INTERMEDIATE(BigDecimal("10")),

    // Food, books, energy
    REDUCED(BigDecimal("5.5")),

    // Medicines refunded by the social security, the press
    SUPER_REDUCED(BigDecimal("2.1"))
}
