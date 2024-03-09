package ru.nasavasa.restaurant.data.models

import com.awazor.cinema.exception.RestaurantException
import ru.nasavasa.restaurant.data.interfaces.SerializableInterface

class Review : SerializableInterface {
    constructor(mark: Int, comment: String) {
        this.mark = mark
        this.comment = comment
    }

    private var _id: Int = 0
    private var _mark: Int = 0
    private var _comment: String = ""

    override var id: Int
        get() {
            return _id
        }
        set(value) {
            _id = value
        }

    var mark: Int
        get() {
            return _mark
        }
        set(value) {
            if (value in 1..5) {
                _mark = value
            } else {
                throw RestaurantException("Оценка должна быть от 1 до 5")
            }
        }

    var comment: String
        get() {
            return _comment
        }
        set(value) {
            _comment = value
        }

    override fun toString(): String {
        return "Review(id=$_id, mark=$_mark, comment='$_comment')"
    }
}
