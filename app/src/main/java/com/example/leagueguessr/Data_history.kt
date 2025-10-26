package com.example.leagueguessr

data class Data_history(
    val championImageResId: Int,
    val result: String,
    val date: String,
    val points: Int = 0,
    val championName: String = ""
)