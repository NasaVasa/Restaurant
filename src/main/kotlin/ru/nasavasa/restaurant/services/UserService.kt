/**
 * Сервис для работы с пользователями.
 */
package ru.nasavasa.restaurant.services

import com.awazor.cinema.exception.RestaurantException
import org.springframework.stereotype.Service
import ru.nasavasa.restaurant.data.interfaces.RepositoryInterface
import ru.nasavasa.restaurant.data.models.User
import ru.nasavasa.restaurant.tools.PasswordHasher
import ru.nasavasa.restaurant.tools.TokenGenerator

@Service
class UserService(private val userRepository: RepositoryInterface<User>) {
    private final val _tokenLength = 32

    /**
     * Регистрирует нового пользователя.
     * @param login Логин нового пользователя.
     * @param password Пароль нового пользователя.
     * @param isAdmin Флаг администратора (true - администратор, false - обычный пользователь).
     * @return Токен авторизации нового пользователя.
     */
    fun register(login: String?, password: String?, isAdmin: Boolean?): String {
        if (login == null || password == null) {
            throw RestaurantException("Переданы не все параметры")
        }
        val user = userRepository.readByField("_login", login)
        if (user != null) throw RestaurantException("Пользователь с таким логином уже существует")
        val hashedPassword = PasswordHasher.hashPassword(password)
        val token = TokenGenerator.generateToken(_tokenLength)
        val newUser: User = if (isAdmin == null || isAdmin == false)
            User(login, hashedPassword, false, token)
        else
            User(login, hashedPassword, true, token)
        val isCreate = userRepository.create(newUser)
        if (!isCreate) throw RestaurantException("Ошибка подключения к бд")
        return token
    }

    /**
     * Аутентифицирует пользователя.
     * @param user Пользователь, который аутентифицируется.
     * @return Токен авторизации пользователя.
     */
    fun authenticate(user: User): String {
        if (user.token != null) throw RestaurantException("Пользователь уже авторизирован")
        val newToken = TokenGenerator.generateToken(_tokenLength)
        user.token = newToken
        val isUpdate = userRepository.update(user)
        if (!isUpdate) throw RestaurantException("Ошибка подключения к бд")
        return newToken
    }

    /**
     * Выход пользователя из системы.
     * @param user Пользователь, который выходит из системы.
     * @return true, если выход прошел успешно, иначе false.
     */
    fun logout(user: User): Boolean {
        if (user.token == null) throw RestaurantException("Пользователь не авторизирован")
        user.token = null
        val isUpdate = userRepository.update(user)
        if (!isUpdate) throw RestaurantException("Ошибка подключения к бд")
        return true
    }

    /**
     * Получает список всех пользователей.
     * @param user Пользователь, запрашивающий список пользователей (должен быть администратором).
     * @return Список всех пользователей.
     */
    fun getUsers(user: User): List<User> {
        if (!user.isAdmin) throw RestaurantException("Пользователь не является администратором")
        val users = userRepository.readAll()
        return users
    }

    /**
     * Получает пользователя по логину и паролю.
     * @param login Логин пользователя.
     * @param password Пароль пользователя.
     * @return Пользователь.
     */
    fun getUser(login: String?, password: String?): User {
        if (login == null || password == null) throw RestaurantException("Переданы не все параметры")
        val user = userRepository.readByField("_login", login)
            ?: throw RestaurantException("Пользователь с таким логином не существует")
        if (!PasswordHasher.verifyPassword(password, user.password)) throw RestaurantException("Неверный пароль")
        return user
    }

    /**
     * Получает пользователя по токену авторизации.
     * @param token Токен авторизации пользователя.
     * @return Пользователь.
     */
    fun getUser(token: String?): User {
        if (token == null) throw RestaurantException("Передан пустой токен")
        val user = userRepository.readByField("_token", token)
            ?: throw RestaurantException("Пользователь с таким токеном не существует")
        return user
    }
}
