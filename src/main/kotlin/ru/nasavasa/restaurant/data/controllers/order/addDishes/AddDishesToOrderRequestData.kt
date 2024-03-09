package ru.nasavasa.restaurant.data.controllers.order.addDishes

import ru.nasavasa.restaurant.data.models.OrderPosition

data class AddDishesToOrderRequestData(
    val token: String?,
    val orderId: Int?,
    val orderList: List<OrderPosition>?,
)