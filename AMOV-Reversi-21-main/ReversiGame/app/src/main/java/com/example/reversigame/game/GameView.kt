package com.example.reversigame.game

import com.example.reversigame.model.Place
import com.example.reversigame.model.Stone

interface GameView {
    open fun putStone(place: Place)
    open fun putBomb(player : Stone,place: Place)
    open fun setCoord(x :  Int, y : Int, count : Int)
    open fun putExchange(player : Stone,place: Place)
    open fun setCurrentPlayerText(player: Stone)
    open fun showWinner(player: Stone, blackCount: Int, whiteCount: Int)
    open fun finishGame()
    open fun markCanPutPlaces(places: List<Place>)
    open fun clearAllMarkPlaces()
}