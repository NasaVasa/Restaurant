package ru.nasavasa.restaurant.data.controllers.order.all

import ru.nasavasa.restaurant.data.models.Order

data class GetOrdersResponseData(val message: String, val orders: List<Order>?)
