package ru.nasavasa.restaurant

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.nasavasa.restaurant.data.interfaces.RepositoryInterface
import ru.nasavasa.restaurant.data.models.Dish
import ru.nasavasa.restaurant.data.models.Order
import ru.nasavasa.restaurant.data.models.User
import ru.nasavasa.restaurant.data.repositories.DishRepository
import ru.nasavasa.restaurant.data.repositories.OrderRepository
import ru.nasavasa.restaurant.data.repositories.UserRepository

@Configuration
class Configuration {
    @Bean
    fun userRepository(): RepositoryInterface<User> {
        return UserRepository()
    }

    @Bean
    fun dishRepository(): RepositoryInterface<Dish> {
        return DishRepository()
    }

    @Bean
    fun orderRepository(): RepositoryInterface<Order> {
        return OrderRepository()
    }

}