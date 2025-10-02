package com.example.reversigame.model.ai

import com.example.reversigame.model.Game
import com.example.reversigame.model.Place
import com.example.reversigame.model.Stone

class AINone : OseroAI {

    override fun computeNext(game: Game, color: Stone): Place {
        throw IllegalAccessException()
    }

}