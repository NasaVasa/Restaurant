package ru.nasavasa.restaurant.data.controllers.restaurant

import ru.nasavasa.restaurant.data.models.Order
import ru.nasavasa.restaurant.data.models.Restaurant

data class RestaurantStatisticResponseData(
    val message: String,
    val restaurant: Restaurant? = null,
    val mostOrderedDishes: Map<Int, Int>? = null,
    val ordersSortedByRating: List<Order>? = null,
    val averageOrderPrice: Double? = null,
)
