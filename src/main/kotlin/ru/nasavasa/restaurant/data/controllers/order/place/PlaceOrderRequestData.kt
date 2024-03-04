package ru.nasavasa.restaurant.data.controllers.order.place

import ru.nasavasa.restaurant.data.models.OrderPosition

class PlaceOrderRequestData(
    val token: String?,
    val orderList: List<OrderPosition>?,
)
