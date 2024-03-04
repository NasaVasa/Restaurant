package com.awazor.cinema.exception

/**
 * Класс исключения, возникающего при работе с рестораном.
 */
class RestaurantException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}