package ru.nasavasa.restaurant.data.controllers.order.removeDishes

import ru.nasavasa.restaurant.data.models.OrderPosition

data class RemoveDishFromOrderRequestData(
    val token: String?,
    val orderId: Int?,
    val orderList: List<OrderPosition>?,
)