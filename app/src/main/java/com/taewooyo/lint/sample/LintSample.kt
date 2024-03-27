package com.taewooyo.lint.sample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LintSample(
    @SerialName("cars") val cars: List<Car>,
    @SerialName("message") val message: String
)

@Serializable
data class Car(
    @SerialName("name") val name: String,
    val number: String
)