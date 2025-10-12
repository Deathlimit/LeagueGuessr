package com.example.leagueguessr

data class Data_champion(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val roles: List<String> = emptyList()
)