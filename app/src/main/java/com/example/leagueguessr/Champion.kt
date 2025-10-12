package com.example.leagueguessr

data class Champion(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val roles: List<String> = emptyList()
)