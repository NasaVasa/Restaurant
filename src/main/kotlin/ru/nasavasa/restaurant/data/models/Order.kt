package ru.nasavasa.restaurant.data.models

import com.awazor.cinema.exception.RestaurantException
import ru.nasavasa.restaurant.data.enums.OrderStatus
import ru.nasavasa.restaurant.data.interfaces.SerializableInterface
import java.util.*

class Order : SerializableInterface {
    constructor(userId: Int, status: OrderStatus, startTime: Date = Date(System.currentTimeMillis())) {
        this.userId = userId
        this.startTime = startTime
        this.status = status
        this.dishes = mutableListOf()
    }

    private var _id: Int = 0
    private var _userId: Int = 0
    private lateinit var _startTime: Date
    private lateinit var _status: OrderStatus
    private lateinit var _dishes: MutableList<OrderPosition>
    private var _price: Double = 0.0
    private var _review: Review? = null

    override var id: Int
        get() {
            return _id
        }
        set(value) {
            _id = value
        }
    var userId: Int
        get() {
            return _userId
        }
        set(value) {
            _userId = value
        }
    var startTime: Date
        get() {
            return _startTime
        }
        set(value) {
            _startTime = value
        }
    var status: OrderStatus
        get() {
            return _status
        }
        set(value) {
            _status = value
        }
    var dishes: List<OrderPosition>
        get() {
            return _dishes
        }
        private set(value) {
            _dishes = value.toMutableList()
        }
    var price: Double
        get() {
            return _price
        }
        set(value) {
            _price = value
        }

    var review: Review?
        get() {
            return _review
        }
        set(value) {
            _review = value
        }

    fun addDish(orderPosition: OrderPosition) {
        val existingDish = _dishes.find { it.dishId == orderPosition.dishId }
        if (existingDish != null) {
            val newQuantity = existingDish.quantity + orderPosition.quantity
            if (newQuantity <= 0) throw RestaurantException("Количество блюд должно быть положительным")
            _dishes.remove(existingDish)
            _dishes.add(OrderPosition(orderPosition.dishId, newQuantity))
        } else {
            if (orderPosition.quantity <= 0) throw RestaurantException("Количество блюд должно быть положительным")
            _dishes.add(OrderPosition(orderPosition.dishId, orderPosition.quantity))
        }
    }

    fun removeDish(orderPosition: OrderPosition) {
        val existingDish = _dishes.find { it.dishId == orderPosition.dishId }
        if (existingDish != null) {
            if (existingDish.quantity - orderPosition.quantity < 0) throw RestaurantException("Из заказа нельзя удалить ${orderPosition.quantity} блюд, так как там их всего ${existingDish.quantity}")
            val newQuantity = existingDish.quantity - orderPosition.quantity
            if (newQuantity > 0) {
                _dishes.remove(existingDish)
                _dishes.add(OrderPosition(orderPosition.dishId, newQuantity))
            } else {
                _dishes.remove(existingDish)
            }
        }
    }

    override fun toString(): String {
        return "Order(id=$_id, userId=$_userId, startTime=$_startTime, status=$_status, dishes=$_dishes, price=$_price, review=$_review)"
    }
}
