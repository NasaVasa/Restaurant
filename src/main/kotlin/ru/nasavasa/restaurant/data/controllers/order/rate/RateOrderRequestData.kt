package ru.nasavasa.restaurant.data.controllers.order.rate

data class RateOrderRequestData(
    val token: String?,
    val orderId: Int?,
    var mark: Int?,
    var comment: String?,
)