/**
 * Сервис для обработки заказов.
 */
package ru.nasavasa.restaurant.services

import com.awazor.cinema.exception.RestaurantException
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.nasavasa.restaurant.data.enums.OrderStatus
import ru.nasavasa.restaurant.data.interfaces.RepositoryInterface
import ru.nasavasa.restaurant.data.models.Dish
import ru.nasavasa.restaurant.data.models.Order
import ru.nasavasa.restaurant.data.models.Restaurant
import ru.nasavasa.restaurant.data.models.User
import java.util.*
import java.util.concurrent.Semaphore

@Service
class OrderProcessingService(
    private val orderRepository: RepositoryInterface<Order>,
    private val dishRepository: RepositoryInterface<Dish>,
    @Value("\${restaurant.workers}") final val restaurantWorkers: Int,
) {
    private val logger: Logger = LoggerFactory.getLogger(OrderProcessingService::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val semaphore = Semaphore(restaurantWorkers)

    /**
     * Контекст обработки заказа, содержащий идентификатор заказа и задание.
     */
    private data class OrderProcessingContext(val orderId: Int, val job: Job)

    private val orderQueue: Queue<Order> = LinkedList()

    private val orderProcessingContextMap = HashMap<Int, OrderProcessingContext>()

    /**
     * Обрабатывает заказ и добавляет его в очередь.
     * @param order Заказ для обработки.
     * @return Идентификатор обработанного заказа.
     */
    fun processOrder(order: Order): Int {
        orderQueue.offer(order) // Добавляем заказ в конец очереди
        processNextOrder() // Начинаем обработку следующего заказа
        return order.id
    }

    private fun processNextOrder() {
        if (orderQueue.isNotEmpty()) {
            val order = orderQueue.poll()
            val orderId = order.id
            val job = coroutineScope.launch {
                try {
                    semaphore.acquire()
                    var allTimeSeconds: Long = 0
                    for (orderPosition in order.dishes) {
                        val dish = dishRepository.read(orderPosition.dishId)
                            ?: throw RestaurantException("В меню нет позиции с id ${orderPosition.dishId}")
                        allTimeSeconds += orderPosition.quantity * dish.cookingTimeMinutes * 60
                    }
                    logger.info("Processing order: $order. It will take ${allTimeSeconds / 60} minutes")
                    order.status = OrderStatus.PREPARING
                    orderRepository.update(order)
                    delay(allTimeSeconds * 1000 / 60)
                    order.status = OrderStatus.READY
                    orderRepository.update(order)
                    val restaurant = Restaurant.getInstance()
                    restaurant.updateRevenue(order.price)
                    logger.info("Order processed: $order")
                } catch (e: CancellationException) {
                    logger.info("Order processing canceled: $order")
                } finally {
                    semaphore.release()
                    processNextOrder()
                }
            }
            val orderProcessingContext = OrderProcessingContext(orderId, job)
            orderProcessingContextMap[orderId] = orderProcessingContext
        }
    }

    /**
     * Отменяет заказ.
     * @param orderId Идентификатор заказа для отмены.
     */
    fun cancel(orderId: Int) {
        val orderProcessingContext = orderProcessingContextMap[orderId]
            ?: throw RestaurantException("Заказ с id $orderId не находится в процессе приготовления")
        val order = orderRepository.read(orderId)
            ?: throw RestaurantException("Заказ с id $orderId не найден")
        if (order.status != OrderStatus.PREPARING) throw RestaurantException("Заказ с id $orderId не находится в процессе приготовления")
        orderProcessingContext.job.cancel()
        order.status = OrderStatus.CANCELED
        orderRepository.update(order)
        orderProcessingContextMap.remove(orderId)
    }

    /**
     * Обновляет количество работников в ресторане.
     * @param user Пользователь, запрашивающий обновление.
     * @return true, если обновление успешно, иначе false.
     */
    fun updateWorkers(user: User): Boolean {
        if (!user.isAdmin) throw RestaurantException("Пользователь ${user.login} не является администратором")
        val restaurant = Restaurant.getInstance()
        restaurant.workers = restaurantWorkers
        return true
    }
}
