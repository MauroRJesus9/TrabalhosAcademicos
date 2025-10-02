package com.example.reversigame.model.ai

import com.example.reversigame.model.Game
import com.example.reversigame.model.Place
import com.example.reversigame.model.Stone
import java.io.Serializable

interface OseroAI : Serializable {

    fun computeNext(game: Game, color: Stone): Place
}