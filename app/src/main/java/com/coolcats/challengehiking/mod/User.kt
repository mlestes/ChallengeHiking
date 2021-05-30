package com.coolcats.challengehiking.mod

data class User(
    val id: String,
    val name: String,
    var miles: Float,
    var hours: Float,
    var hikes: Int,
    var challenges: Int,
    var lastHiked: String
) {
    constructor() : this("", "", 0.0F, 0.0F,0, 0, "")
}
