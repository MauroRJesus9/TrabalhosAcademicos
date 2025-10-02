package com.example.reversigame.model

enum class Stone {

    BLACK, WHITE, NONE;

    fun other() = if (this == BLACK) WHITE else BLACK
}