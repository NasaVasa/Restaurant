package ru.nasavasa.restaurant.controllers


import com.awazor.cinema.exception.RestaurantException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.nasavasa.restaurant.data.controllers.order.addDishes.AddDishesToOrderRequestData
import ru.nasavasa.restaurant.data.controllers.order.addDishes.AddDishesToOrderResponseData
import ru.nasavasa.restaurant.data.controllers.order.all.GetOrdersResponseData
import ru.nasavasa.restaurant.data.controllers.order.cancel.CancelOrderRequestData
import ru.nasavasa.restaurant.data.controllers.order.cancel.CancelOrderResponseData
import ru.nasavasa.restaurant.data.controllers.order.pay.PayOrderRequestData
import ru.nasavasa.restaurant.data.controllers.order.pay.PayOrderResponseData
import ru.nasavasa.restaurant.data.controllers.order.pay.RateOrderResponseData
import ru.nasavasa.restaurant.data.controllers.order.place.PlaceOrderRequestData
import ru.nasavasa.restaurant.data.controllers.order.place.PlaceOrderResponseData
import ru.nasavasa.restaurant.data.controllers.order.rate.RateOrderRequestData
import ru.nasavasa.restaurant.data.controllers.order.removeDishes.RemoveDishFromOrderRequestData
import ru.nasavasa.restaurant.data.controllers.order.removeDishes.RemoveDishFromOrderResponseData
import ru.nasavasa.restaurant.services.OrderService
import ru.nasavasa.restaurant.services.UserService

@RestController
@RequestMapping("/order")
class OrderController(
    private val userService: UserService,
    private val orderService: OrderService,
) {
    private val logger: Logger = LoggerFactory.getLogger(OrderController::class.java)

    @PostMapping("/place")
    fun placeOrder(
        @RequestBody requestData: PlaceOrderRequestData,
    ): ResponseEntity<PlaceOrderResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            val orderId = orderService.placeOrder(
                user, requestData.orderList
            )
            logger.info("Add ${requestData.orderList} order by ${user.login}")
            ResponseEntity.ok(PlaceOrderResponseData("Заказ добавлен", orderId))
        } catch (e: RestaurantException) {
            logger.error("Error during add dish: $e")
            ResponseEntity.badRequest().body(PlaceOrderResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during add dish: $e")
            ResponseEntity.badRequest().body(PlaceOrderResponseData("Неизвестная ошибка"))
        }
    }

    @PostMapping("/add-to-order")
    fun addDishesToOrder(
        @RequestBody requestData: AddDishesToOrderRequestData,
    ): ResponseEntity<AddDishesToOrderResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            orderService.addToOrder(
                user, requestData.orderId, requestData.orderList
            )
            logger.info("Add ${requestData.orderList} to order ${requestData.orderId} by ${user.login}")
            ResponseEntity.ok(AddDishesToOrderResponseData("Блюда были успешно добавлены в заказ"))
        } catch (e: RestaurantException) {
            logger.error("Error during add dishes to order: $e")
            ResponseEntity.badRequest().body(AddDishesToOrderResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during add dishes to order: $e")
            ResponseEntity.badRequest().body(AddDishesToOrderResponseData("Неизвестная ошибка"))
        }
    }

    @DeleteMapping("/remove-from-order")
    fun removeDishesToOrder(
        @RequestBody requestData: RemoveDishFromOrderRequestData,
    ): ResponseEntity<RemoveDishFromOrderResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            orderService.removeFromOrder(
                user, requestData.orderId, requestData.orderList
            )
            logger.info("Add ${requestData.orderList} to order ${requestData.orderId} by ${user.login}")
            ResponseEntity.ok(RemoveDishFromOrderResponseData("Блюда были удалены из заказа"))
        } catch (e: RestaurantException) {
            logger.error("Error during remove dishes from order: $e")
            ResponseEntity.badRequest().body(RemoveDishFromOrderResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during remove dishes from order: $e")
            ResponseEntity.badRequest().body(RemoveDishFromOrderResponseData("Неизвестная ошибка"))
        }
    }

    @PostMapping("/cancel")
    fun cancelOrder(
        @RequestBody requestData: CancelOrderRequestData,
    ): ResponseEntity<CancelOrderResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            orderService.cancelOrder(
                user, requestData.orderId
            )
            logger.info("Cancel order ${requestData.orderId} by ${user.login}")
            ResponseEntity.ok(CancelOrderResponseData("Заказ был отменён"))
        } catch (e: RestaurantException) {
            logger.error("Error during cancel order: $e")
            ResponseEntity.badRequest().body(CancelOrderResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during cancel order: $e")
            ResponseEntity.badRequest().body(CancelOrderResponseData("Неизвестная ошибка"))
        }
    }

    @PostMapping("/pay")
    fun payOrder(
        @RequestBody requestData: PayOrderRequestData,
    ): ResponseEntity<PayOrderResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            orderService.payOrder(
                user, requestData.orderId
            )
            logger.info("Order ${requestData.orderId} payed by ${user.login}")
            ResponseEntity.ok(PayOrderResponseData("Заказ был оплачен"))
        } catch (e: RestaurantException) {
            logger.error("Error during pay order: $e")
            ResponseEntity.badRequest().body(PayOrderResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during pay order: $e")
            ResponseEntity.badRequest().body(PayOrderResponseData("Неизвестная ошибка"))
        }
    }

    @GetMapping("/all")
    fun getAllDishes(
        @RequestParam token: String?,
    ): ResponseEntity<GetOrdersResponseData> {
        return try {
            val user = userService.getUser(token)
            val orders = orderService.getOrders(user)
            logger.info("Get orders by ${user.login}")
            ResponseEntity.ok(GetOrdersResponseData("Успешно получена информация обо всех заказах", orders))
        } catch (e: RestaurantException) {
            logger.error("Error during get orders: $e")
            ResponseEntity.badRequest().body(GetOrdersResponseData(e.message.toString(), null))
        } catch (e: Exception) {
            logger.error("Unknown error during get orders: $e")
            ResponseEntity.badRequest().body(GetOrdersResponseData("Неизвестная ошибка", null))
        }
    }

    @PostMapping("/rate")
    fun rateOrder(
        @RequestBody requestData: RateOrderRequestData,
    ): ResponseEntity<RateOrderResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            orderService.rateOrder(
                user, requestData.orderId, requestData.mark, requestData.comment
            )
            logger.info("Order ${requestData.orderId} rated by ${user.login}")
            ResponseEntity.ok(RateOrderResponseData("Заказ был оценён"))
        } catch (e: RestaurantException) {
            logger.error("Error during rate order: $e")
            ResponseEntity.badRequest().body(RateOrderResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during rate order: $e")
            ResponseEntity.badRequest().body(RateOrderResponseData("Неизвестная ошибка"))
        }
    }
}
