package org.flexstore.mcp

val deskLamp = Product(
    id = "desk-lamp",
    sku = "HOM-1",
    name = "Desk lamp",
    description = "A lamp for the desk",
    category = "Home",
    price = 35.0,
    currency = "EUR",
    currentPrice = 35.0
)

val deskLampOnPromotion = deskLamp.copy(promotionPercentage = 20.0, currentPrice = 28.0)

val lampeDeBureau = deskLamp.copy(name = "Lampe de bureau", description = "Une lampe pour le bureau")
