package ru.nasavasa.restaurant.data.controllers.healthcheck

import ru.nasavasa.restaurant.data.enums.ApplicationStatus
import java.util.*

class HealthCheckResponseData(
    val message: String, val status: ApplicationStatus, val version: String, val date: Date = Date(),
)