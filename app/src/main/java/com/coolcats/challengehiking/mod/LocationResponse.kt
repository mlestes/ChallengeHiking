package com.coolcats.challengehiking.mod

data class LocationResponse(
    val plus_code: PlusCode,
    val results: List<Result>,
    val status: String
)