package ru.nasavasa.restaurant.data.interfaces

import com.google.gson.Gson

interface SerializableInterface {
    companion object {
        val gson = Gson()
    }

    var id: Int
    fun serialize(): String {
        return gson.toJson(this)
    }

    fun deserialize(data: String): SerializableInterface {
        return gson.fromJson(data, this::class.java)
    }
}