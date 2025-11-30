package me.znotchill.guncore.gun.classes

import kotlinx.serialization.Serializable

@Serializable
data class RGBColor(
    val r: Int,
    val g: Int,
    val b: Int
)