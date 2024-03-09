/**
 * Сервис для работы с заказами.
 */
package ru.nasavasa.restaurant.services

import com.awazor.cinema.exception.RestaurantException
import org.springframework.stereotype.Service
import ru.nasavasa.restaurant.data.enums.OrderStatus
import ru.nasavasa.restaurant.data.interfaces.RepositoryInterface
import ru.nasavasa.restaurant.data.models.*

@Service
class OrderService(
    private val orderRepository: RepositoryInterface<Order>,
    private val dishRepository: RepositoryInterface<Dish>,
    private val orderProcessingService: OrderProcessingService,
) {
    /**
     * Создает новый заказ.
     * @param user Пользователь, размещающий заказ.
     * @param orderList Список позиций заказа.
     * @return Идентификатор созданного заказа.
     */
    fun placeOrder(user: User, orderList: List<OrderPosition>?): Int {
        if (orderList == null) throw RestaurantException("Заказ не может быть пустым")
        val newOrder = Order(user.id, OrderStatus.ACCEPTED)
        for (orderPosition in orderList) {
            val dish = dishRepository.read(orderPosition.dishId)
                ?: throw RestaurantException("В меню нет позиции с id ${orderPosition.dishId}")
            if (orderPosition.quantity > dish.quantity) throw RestaurantException("Вы не можете заказать ${orderPosition.quantity} порций, так как в меню их всего ${dish.quantity}")
            dish.quantity -= orderPosition.quantity
            dishRepository.update(dish)
            newOrder.addDish(orderPosition)
            newOrder.price += dish.price * orderPosition.quantity
        }
        val isCreated = orderRepository.create(newOrder)
        if (!isCreated) throw RestaurantException("Ошибка подключения к бд")
        val id = orderProcessingService.processOrder(newOrder)
        return id
    }

    /**
     * Добавляет позиции в существующий заказ.
     * @param user Пользователь, добавляющий позиции в заказ.
     * @param orderId Идентификатор заказа.
     * @param orderList Список позиций для добавления.
     * @return true, если позиции успешно добавлены, иначе false.
     */
    fun addToOrder(user: User, orderId: Int?, orderList: List<OrderPosition>?): Boolean {
        if (orderId == null) throw RestaurantException("Не указан id заказа")
        if (orderList == null) throw RestaurantException("Заказ не может быть пустым")
        val order = orderRepository.read(orderId) ?: throw RestaurantException("Заказ с id $orderId не найден")
        if (order.status == OrderStatus.PREPARING) throw RestaurantException("Заказ находится в процессе приготовления")
        if (order.status == OrderStatus.READY) throw RestaurantException("Заказ уже готов")
        if (order.userId != user.id && !user.isAdmin) throw RestaurantException("Заказ с id $orderId не принадлежит пользователю ${user.login} и пользователь не является администратором")
        for (orderPosition in orderList) {
            val dish = dishRepository.read(orderPosition.dishId)
                ?: throw RestaurantException("В меню нет позиции с id ${orderPosition.dishId}")
            dish.quantity -= orderPosition.quantity
            dishRepository.update(dish)
            order.addDish(orderPosition)
            order.price += dish.price * orderPosition.quantity
        }
        val isUpdated = orderRepository.update(order)
        if (!isUpdated) throw RestaurantException("Ошибка подключения к бд")
        return true
    }

    /**
     * Удаляет позиции из существующего заказа.
     * @param user Пользователь, удаляющий позиции из заказа.
     * @param orderId Идентификатор заказа.
     * @param orderList Список позиций для удаления.
     * @return true, если позиции успешно удалены, иначе false.
     */
    fun removeFromOrder(user: User, orderId: Int?, orderList: List<OrderPosition>?): Boolean {
        if (orderId == null) throw RestaurantException("Не указан id заказа")
        if (orderList == null) throw RestaurantException("Заказ не может быть пустым")
        val order = orderRepository.read(orderId) ?: throw RestaurantException("Заказ с id $orderId не найден")
        if (order.userId != user.id && !user.isAdmin) throw RestaurantException("Заказ с id $orderId не принадлежит пользователю ${user.login} и пользователь не является администратором")
        if (order.status == OrderStatus.PREPARING) throw RestaurantException("Заказ находится в процессе приготовления")
        if (order.status == OrderStatus.READY) throw RestaurantException("Заказ уже готов")
        for (orderPosition in orderList) {
            val actualOrderPosition = order.dishes.firstOrNull { it.dishId == orderPosition.dishId }
            if (actualOrderPosition == null) throw RestaurantException("В заказе нет позиции с id ${orderPosition.dishId}")
            val dish = dishRepository.read(orderPosition.dishId)
                ?: throw RestaurantException("В меню нет позиции с id ${orderPosition.dishId}")
            dish.quantity += orderPosition.quantity
            dishRepository.update(dish)
            orderRepository.update(order)
            order.removeDish(orderPosition)
            order.price -= dish.price * orderPosition.quantity
        }
        val isUpdated = orderRepository.update(order)
        if (!isUpdated) throw RestaurantException("Ошибка подключения к бд")
        return true
    }

    /**
     * Отменяет заказ.
     * @param user Пользователь, отменяющий заказ.
     * @param orderId Идентификатор заказа.
     * @return true, если заказ успешно отменен, иначе false.
     */
    fun cancelOrder(user: User, orderId: Int?): Boolean {
        if (orderId == null) throw RestaurantException("Не указан id заказа")
        val order = orderRepository.read(orderId) ?: throw RestaurantException("Заказ с id $orderId не найден")
        if (order.userId != user.id && !user.isAdmin) throw RestaurantException("Заказ с id $orderId не принадлежит пользователю ${user.login} и пользователь не является администратором")
        if (order.status == OrderStatus.READY) throw RestaurantException("Заказ уже готов")
        orderProcessingService.cancel(orderId)
        return true
    }

    /**
     * Оплачивает заказ.
     * @param user Пользователь, оплачивающий заказ.
     * @param orderId Идентификатор заказа.
     * @return true, если заказ успешно оплачен, иначе false.
     */
    fun payOrder(user: User, orderId: Int?): Boolean {
        if (orderId == null) throw RestaurantException("Не указан id заказа")
        val order = orderRepository.read(orderId) ?: throw RestaurantException("Заказ с id $orderId не найден")
        if (order.userId != user.id && !user.isAdmin) throw RestaurantException("Заказ с id $orderId не принадлежит пользователю ${user.login} и пользователь не является администратором")
        if (order.status == OrderStatus.PAID) throw RestaurantException("Заказ уже оплачен")
        if (order.status == OrderStatus.CANCELED) throw RestaurantException("Заказ был отменён")
        if (order.status != OrderStatus.READY) throw RestaurantException("Заказ ещё не готов")
        order.status = OrderStatus.PAID
        val isUpdated = orderRepository.update(order)
        if (!isUpdated) throw RestaurantException("Ошибка подключения к бд")
        return true
    }

    /**
     * Отмечает заказ оценкой и комментарием.
     * @param user Пользователь, оценивающий заказ.
     * @param orderId Идентификатор заказа.
     * @param mark Оценка заказа.
     * @param comment Комментарий к заказу.
     * @return true, если заказ успешно оценен, иначе false.
     * @throws RestaurantException если переданы некорректные параметры или возникают ошибки при работе с базой данных.
     */

    fun rateOrder(user: User, orderId: Int?, mark: Int?, comment: String?): Boolean {
        if (orderId == null) throw RestaurantException("Не указан id заказа")
        if (mark == null) throw RestaurantException("Не поставлена оценка")
        if (comment == null) throw RestaurantException("Не оставлен комментарий")
        val order = orderRepository.read(orderId) ?: throw RestaurantException("Заказ с id $orderId не найден")
        if (order.userId != user.id && !user.isAdmin) throw RestaurantException("Заказ с id $orderId не принадлежит пользователю ${user.login} и пользователь не является администратором")
        if (order.status != OrderStatus.PAID) throw RestaurantException("Заказ не оплачен")
        val review = Review(mark, comment)
        order.review = review
        val isUpdated = orderRepository.update(order)
        if (!isUpdated) throw RestaurantException("Ошибка подключения к бд")
        return true
    }

    /**
     * Возвращает список заказов.
     * @param user Пользователь, для которого возвращается список заказов.
     * @return Список заказов пользователя или всех заказов, если пользователь - администратор.
     * @throws RestaurantException если пользователь не является администратором или возникают ошибки при работе с базой данных.
     */
    fun getOrders(user: User): List<Order> {
        return if (user.isAdmin) {
            orderRepository.readAll()
        } else {
            orderRepository.readAll().filter { it.userId == user.id }
        }
    }

    /**
     * Обновляет общий доход ресторана.
     * @param user Пользователь, обновляющий общий доход.
     * @return true, если общий доход успешно обновлен, иначе false.
     * @throws RestaurantException если пользователь не является администратором или возникают ошибки при работе с базой данных.
     */
    fun updateTotalRevenue(user: User): Boolean {
        if (!user.isAdmin) throw RestaurantException("Пользователь ${user.login} не является администратором")
        val totalRevenue = orderRepository.readAll().filter { it.status == OrderStatus.PAID }.sumOf { it.price }
        val restaurant = Restaurant.getInstance()
        restaurant.totalRevenue = totalRevenue
        return true
    }

    /**
     * Возвращает карту, содержащую информацию о самых заказываемых блюдах.
     * @return Карта, где ключ - идентификатор блюда, а значение - количество заказанных порций.
     */

    fun mostOrderedDishes(): Map<Int, Int> {
        val allOrders = orderRepository.readAll()
        val dishCountMap = mutableMapOf<Int, Int>()
        allOrders.forEach { order ->
            order.dishes.forEach { orderPosition ->
                val dishId = orderPosition.dishId
                dishCountMap[dishId] = dishCountMap.getOrDefault(dishId, 0) + orderPosition.quantity
            }
        }
        return dishCountMap.toList().sortedByDescending { (_, quantity) -> quantity }.toMap()
    }

    /**
     * Возвращает список заказов, отсортированный по рейтингу.
     * @return Список заказов, отсортированный по убыванию рейтинга.
     */

    fun ordersSortedByRating(): List<Order> {
        return orderRepository.readAll().filter { it.review != null }.sortedByDescending { it.review?.mark }
    }

    /**
     * Возвращает среднюю стоимость заказа.
     * @return Средняя стоимость заказа.
     */
    fun averageOrderPrice(): Double {
        val allOrders = orderRepository.readAll()
        val totalAmount = allOrders.sumOf { it.price }
        return if (allOrders.isNotEmpty()) totalAmount / allOrders.size else 0.0
    }


}
