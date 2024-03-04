package ru.nasavasa.restaurant.data.controllers.order.pay

data class PayOrderRequestData(
    val token: String?,
    val orderId: Int?,
)