package ru.nasavasa.restaurant.controllers

import com.awazor.cinema.exception.RestaurantException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.nasavasa.restaurant.data.controllers.dish.add.AddDishRequestData
import ru.nasavasa.restaurant.data.controllers.dish.add.AddDishResponseData
import ru.nasavasa.restaurant.data.controllers.dish.all.GetDishesResponseData
import ru.nasavasa.restaurant.data.controllers.dish.delete.DeleteDishRequestData
import ru.nasavasa.restaurant.data.controllers.dish.delete.DeleteDishResponseData
import ru.nasavasa.restaurant.services.DishService
import ru.nasavasa.restaurant.services.UserService

@RestController
@RequestMapping("/dish")
class DishController(private val userService: UserService, private val dishService: DishService) {
    private val logger: Logger = LoggerFactory.getLogger(DishController::class.java)

    @PostMapping("/add")
    fun addDish(
        @RequestBody requestData: AddDishRequestData,
    ): ResponseEntity<AddDishResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            dishService.addDish(
                user,
                requestData.dishName,
                requestData.dishDescription,
                requestData.dishQuantity,
                requestData.dishCookingTimeMinutes,
                requestData.dishPrice
            )
            logger.info("Add ${requestData.dishName} dish by ${user.login}")
            ResponseEntity.ok(AddDishResponseData("Блюдо добавлено"))
        } catch (e: RestaurantException) {
            logger.error("Error during add dish: $e")
            ResponseEntity.badRequest().body(AddDishResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during add dish: $e")
            ResponseEntity.badRequest().body(AddDishResponseData("Неизвестная ошибка"))
        }
    }

    @DeleteMapping("/delete")
    fun deleteDish(
        @RequestBody requestData: DeleteDishRequestData,
    ): ResponseEntity<DeleteDishResponseData> {
        return try {
            val user = userService.getUser(requestData.token)
            dishService.deleteDish(user, requestData.dishId)
            logger.info("Delete dish id ${requestData.dishId} by ${user.login}")
            ResponseEntity.ok(DeleteDishResponseData("Блюдо удалено"))
        } catch (e: RestaurantException) {
            logger.error("Error during delete dish: $e")
            ResponseEntity.badRequest().body(DeleteDishResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during delete dish: $e")
            ResponseEntity.badRequest().body(DeleteDishResponseData("Неизвестная ошибка"))
        }
    }

    @GetMapping("/all")
    fun getAllDishes(
        @RequestParam token: String?,
    ): ResponseEntity<GetDishesResponseData> {
        return try {
            val user = userService.getUser(token)
            val dishes = dishService.getDishes(user)
            logger.info("Get dishes by ${user.login}")
            ResponseEntity.ok(GetDishesResponseData("Успешно получена информация о всех блюдах", dishes))
        } catch (e: RestaurantException) {
            logger.error("Error during get dishes: $e")
            ResponseEntity.badRequest().body(GetDishesResponseData(e.message.toString(), null))
        } catch (e: Exception) {
            logger.error("Unknown error during get dishes: $e")
            ResponseEntity.badRequest().body(GetDishesResponseData("Неизвестная ошибка", null))
        }
    }
}
