package com.coolcats.challengehiking.mod

data class User(val id: String, val name: String, val miles: Float, val hikes: Int, val challenges: Int) {
    constructor() : this("", "", 0.0F, 0, 0)
}
