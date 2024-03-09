package ru.nasavasa.restaurant.data.controllers.order.cancel

data class CancelOrderRequestData(
    val token: String?,
    val orderId: Int?,
)