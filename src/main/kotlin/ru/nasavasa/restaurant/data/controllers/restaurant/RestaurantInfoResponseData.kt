package ru.nasavasa.restaurant.data.controllers.restaurant

import ru.nasavasa.restaurant.data.models.Restaurant

data class RestaurantInfoResponseData(val message: String, val restaurant: Restaurant? = null)
