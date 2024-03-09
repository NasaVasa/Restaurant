package ru.nasavasa.restaurant.data.repositories

import ru.nasavasa.restaurant.data.interfaces.RepositoryInterface
import ru.nasavasa.restaurant.data.models.Dish
import java.io.File

class DishRepository : RepositoryInterface<Dish> {
    companion object {
        /**
         * Path to the database folder.
         */
        private val dbRepository = System.getProperty("user.dir") + System.getProperty("file.separator") +
                "src" + System.getProperty("file.separator") +
                "main" + System.getProperty("file.separator") +
                "kotlin" + System.getProperty("file.separator") +
                "ru" + System.getProperty("file.separator") +
                "nasavasa" + System.getProperty("file.separator") +
                "restaurant" + System.getProperty("file.separator") +
                "database" + System.getProperty("file.separator") +
                "dishes.json"
    }

    override val file = File(dbRepository)

    override fun create(obj: Dish): Boolean {
        val list = readAll().toMutableList()
        obj.id = list.size + 1
        list.add(obj)
        return writeToFile(list)
    }

    override fun read(id: Int): Dish? {
        return readAll().firstOrNull { it.id == id }
    }

    override fun readAll(): List<Dish> {
        return readFromFile(Dish::class.java)
    }

    override fun readByField(fieldName: String, fieldValue: String): Dish? {
        val dishes = readAll()
        return dishes.firstOrNull { it.serialize().contains("\"$fieldName\":\"$fieldValue\"") }
    }

    override fun delete(id: Int): Boolean {
        val list = readAll().toMutableList()
        val objToDelete = list.find { it.id == id }
        objToDelete?.let {
            list.remove(it)
            return writeToFile(list)
        }
        return false
    }

    override fun update(obj: Dish): Boolean {
        val list = readAll().toMutableList()
        val existingObj = list.find { it.id == obj.id }
        existingObj?.let {
            list.remove(it)
            list.add(obj)
            return writeToFile(list)
        }
        return false
    }
}
