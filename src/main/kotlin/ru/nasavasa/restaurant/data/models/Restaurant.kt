package ru.nasavasa.restaurant.data.models

class Restaurant private constructor(
    val name: String,
    val address: String,
    var totalRevenue: Double = 0.0,
    var workers: Int = 0,
) {
    companion object {
        private var instance: Restaurant? = null

        fun getInstance(): Restaurant {
            return instance ?: synchronized(this) {
                instance ?: Restaurant(
                    name = "My Restaurant",
                    address = "123 Main St"
                ).also { instance = it }
            }
        }
    }

    fun updateRevenue(amount: Double) {
        totalRevenue += amount
    }
}
