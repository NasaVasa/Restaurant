package ru.nasavasa.restaurant.controllers

import com.awazor.cinema.exception.RestaurantException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.nasavasa.restaurant.data.controllers.user.all.UsersResponseData
import ru.nasavasa.restaurant.data.controllers.user.auth.AuthRequestData
import ru.nasavasa.restaurant.data.controllers.user.auth.AuthResponseData
import ru.nasavasa.restaurant.data.controllers.user.logout.LogoutRequestData
import ru.nasavasa.restaurant.data.controllers.user.logout.LogoutResponseData
import ru.nasavasa.restaurant.data.controllers.user.register.RegisterRequestData
import ru.nasavasa.restaurant.data.controllers.user.register.RegisterResponseData
import ru.nasavasa.restaurant.services.UserService


@RestController
class UserController(private val userService: UserService) {
    private val logger: Logger = LoggerFactory.getLogger(UserController::class.java)

    @PostMapping("/register")
    fun register(
        @RequestBody registerRequestData: RegisterRequestData,
    ): ResponseEntity<RegisterResponseData> {
        return try {
            val token = userService.register(
                registerRequestData.login, registerRequestData.password, registerRequestData.isAdmin
            )
            logger.info("User ${registerRequestData.login} registered")
            ResponseEntity.ok(RegisterResponseData("Пользователь зарегистрирован", token))
        } catch (e: RestaurantException) {
            logger.error("Error during register: $e")
            ResponseEntity.badRequest().body(RegisterResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during register: $e")
            ResponseEntity.badRequest().body(RegisterResponseData("Неизвестная ошибка"))
        }
    }

    @PostMapping("/auth")
    fun auth(
        @RequestBody authRequestData: AuthRequestData,
    ): ResponseEntity<AuthResponseData> {
        return try {
            val user = userService.getUser(authRequestData.login, authRequestData.password)
            val token = userService.authenticate(user)
            logger.info("User ${authRequestData.login} authenticated")
            ResponseEntity.ok(AuthResponseData("Пользователь вошёл в систему", token))
        } catch (e: RestaurantException) {
            logger.error("Error during authenticate: $e")
            ResponseEntity.badRequest().body(AuthResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during authenticate: $e")
            ResponseEntity.badRequest().body(AuthResponseData("Неизвестная ошибка"))
        }
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody logoutRequestData: LogoutRequestData,
    ): ResponseEntity<LogoutResponseData> {
        return try {
            val user = userService.getUser(logoutRequestData.token)
            userService.logout(user)
            logger.info("User ${user.login} logged out")
            ResponseEntity.ok(LogoutResponseData("Пользователь вышел из системы"))
        } catch (e: RestaurantException) {
            logger.error("Error during logout: $e")
            ResponseEntity.badRequest().body(LogoutResponseData(e.message.toString()))
        } catch (e: Exception) {
            logger.error("Unknown error during logout: $e")
            ResponseEntity.badRequest().body(LogoutResponseData("Неизвестная ошибка"))
        }
    }


    @GetMapping("/users")
    fun users(@RequestParam token: String?): ResponseEntity<UsersResponseData> {
        println(token)
        return try {
            val user = userService.getUser(token)
            val users = userService.getUsers(user)
            logger.info("Get users by ${user.login}")
            ResponseEntity.ok(UsersResponseData("Успешно была получена информация обо всех пользователях", users))
        } catch (e: RestaurantException) {
            logger.error("Error during get users: $e")
            ResponseEntity.badRequest().body(UsersResponseData(e.message.toString(), null))
        } catch (e: Exception) {
            logger.error("Unknown error during get users: $e")
            ResponseEntity.badRequest().body(UsersResponseData("Неизвестная ошибка", null))
        }
    }
}
