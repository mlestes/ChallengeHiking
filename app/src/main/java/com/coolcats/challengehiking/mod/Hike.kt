package com.coolcats.challengehiking.mod

data class Hike(
    val id: String,
    val startLocation: String,
    val endLocation: String,
    val challenges: Int,
    val time: Float,
    val distance: Float,
    val date: String
) {
    constructor() : this("", "", "", 0, 0.0F, 0.0F, "")
}
