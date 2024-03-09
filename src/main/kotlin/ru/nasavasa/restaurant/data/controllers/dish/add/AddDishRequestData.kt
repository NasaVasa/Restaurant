package ru.nasavasa.restaurant.data.controllers.dish.add

data class AddDishRequestData(
    val token: String?,
    val dishName: String?,
    val dishDescription: String?,
    val dishQuantity: Int?,
    val dishCookingTimeMinutes: Int?,
    val dishPrice: Double?,
)
