package ru.nasavasa.restaurant.controllers

import com.awazor.cinema.exception.RestaurantException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.nasavasa.restaurant.data.controllers.restaurant.RestaurantInfoResponseData
import ru.nasavasa.restaurant.data.controllers.restaurant.RestaurantStatisticResponseData
import ru.nasavasa.restaurant.data.models.Restaurant
import ru.nasavasa.restaurant.services.OrderProcessingService
import ru.nasavasa.restaurant.services.OrderService
import ru.nasavasa.restaurant.services.UserService

@RestController
@RequestMapping("/restaurant")
class RestaurantController(
    private val userService: UserService,
    private val orderService: OrderService,
    private val orderProcessingService: OrderProcessingService,
) {
    private val logger: Logger = LoggerFactory.getLogger(RestaurantController::class.java)

    @GetMapping("/info")
    fun restaurantStatus(@RequestParam token: String?): ResponseEntity<RestaurantInfoResponseData> {
        try {
            val user = userService.getUser(token)
            orderService.updateTotalRevenue(user)
            orderProcessingService.updateWorkers(user)
            return ResponseEntity.ok(RestaurantInfoResponseData("Данные о ресторане", Restaurant.getInstance()))
        } catch (e: RestaurantException) {
            logger.error("Error during get restaurant status: $e")
            return ResponseEntity.badRequest().body(RestaurantInfoResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during get restaurant status: $e")
            return ResponseEntity.badRequest().body(RestaurantInfoResponseData("Неизвестная ошибка"))
        }
    }

    @GetMapping("/statistic")
    fun restaurantStatistic(@RequestParam token: String?): ResponseEntity<RestaurantStatisticResponseData> {
        try {
            val user = userService.getUser(token)
            orderService.updateTotalRevenue(user)
            orderProcessingService.updateWorkers(user)
            val mostOrderedDishes = orderService.mostOrderedDishes()
            val ordersSortedByRating = orderService.ordersSortedByRating()
            val averageOrderPrice = orderService.averageOrderPrice()
            return ResponseEntity.ok(
                RestaurantStatisticResponseData(
                    "Статистика ресторана",
                    Restaurant.getInstance(),
                    mostOrderedDishes,
                    ordersSortedByRating,
                    averageOrderPrice
                )
            )
        } catch (e: RestaurantException) {
            logger.error("Error during get restaurant statistic: $e")
            return ResponseEntity.badRequest().body(RestaurantStatisticResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during get restaurant statistic: $e")
            return ResponseEntity.badRequest().body(RestaurantStatisticResponseData("Неизвестная ошибка"))
        }

    }
}
