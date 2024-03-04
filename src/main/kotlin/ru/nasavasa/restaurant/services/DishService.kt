package ru.nasavasa.restaurant.services

import com.awazor.cinema.exception.RestaurantException
import org.springframework.stereotype.Service
import ru.nasavasa.restaurant.data.enums.OrderStatus
import ru.nasavasa.restaurant.data.interfaces.RepositoryInterface
import ru.nasavasa.restaurant.data.models.Dish
import ru.nasavasa.restaurant.data.models.User

@Service
class DishService(private val dishRepository: RepositoryInterface<Dish>, private val orderService: OrderService) {
    fun addDish(
        user: User,
        name: String?,
        description: String?,
        quantity: Int?,
        dishCookingTimeMinutes: Int?,
        price: Double?,
    ): Boolean {
        if (!user.isAdmin) throw RestaurantException("Пользователь не является администратором")
        if (name == null || description == null || quantity == null || dishCookingTimeMinutes == null || price == null) {
            throw RestaurantException("Переданы не все параметры")
        }
        if (quantity <= 0) throw RestaurantException("Количество должно быть положительным")
        if (dishCookingTimeMinutes <= 0) throw RestaurantException("Время приготовления в минутах должно быть положительным")
        if (price < 0) throw RestaurantException("Цена должна быть неотрицательной")
        val newDish = Dish(name, description, quantity, dishCookingTimeMinutes, price)
        val isCreate = dishRepository.create(newDish)
        if (!isCreate) throw RestaurantException("Ошибка подключения к бд")
        return isCreate
    }

    fun deleteDish(user: User, dishId: Int?): Boolean {
        if (!user.isAdmin) throw RestaurantException("Пользователь не является администратором")
        if (dishId == null) throw RestaurantException("Переданы не все параметры")
        val orders = orderService.getOrders(user)
        for (order in orders) {
            if (order.status == OrderStatus.READY) continue
            for (dish in order.dishes) {
                if (dish.dishId == dishId) throw RestaurantException("Вы не можете удалить блюдо, так как оно находится в процессе приготовления")
            }
        }
        val isDelete = dishRepository.delete(dishId)
        if (!isDelete) throw RestaurantException("Неверный id")
        return true
    }

    fun getDishes(user: User): List<Dish> {
        return dishRepository.readAll()
    }

}
