package ru.nasavasa.restaurant.data.interfaces

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

interface RepositoryInterface<T : SerializableInterface> {
    fun create(obj: T): Boolean
    fun read(id: Int): T?
    fun readAll(): List<T>
    fun readByField(fieldName: String, fieldValue: String): T?
    fun update(obj: T): Boolean
    fun delete(id: Int): Boolean

    val file: File

    fun readFromFile(clazz: Class<T>): List<T> {
        if (!file.exists() || file.length() == 0L) {
            return emptyList()
        }
        val content = file.readText()
        val listType = TypeToken.getParameterized(List::class.java, clazz).type
        return Gson().fromJson(content, listType)
    }


    fun writeToFile(list: List<T>): Boolean {
        val text = Gson().toJson(list)
        file.writeText(text)
        return true
    }
}
