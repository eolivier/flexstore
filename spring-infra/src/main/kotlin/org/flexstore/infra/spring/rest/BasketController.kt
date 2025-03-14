package org.flexstore.infra.spring.rest

import org.flexstore.domain.entity.Basket
import org.flexstore.domain.valueobject.Item
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/basket")
class BasketController {

    private val basket = Basket()

    @GetMapping("/items")
    fun getBasketItems(): List<Item> = basket.getItems()

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody newItem: Item) = basket.addOrReplaceItem(newItem)
}