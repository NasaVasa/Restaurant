package ru.nasavasa.restaurant.data.controllers.dish.all

import ru.nasavasa.restaurant.data.models.Dish

data class GetDishesResponseData(val message: String, val dishes: List<Dish>?)
