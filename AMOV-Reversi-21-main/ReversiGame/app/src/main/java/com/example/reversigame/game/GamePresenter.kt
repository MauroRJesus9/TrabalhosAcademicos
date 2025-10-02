package com.example.reversigame.game

import android.util.Log
import android.widget.GridLayout
import androidx.annotation.VisibleForTesting
import com.example.reversigame.R
import com.example.reversigame.model.Game
import com.example.reversigame.model.Place
import com.example.reversigame.model.Stone
import com.example.reversigame.model.ai.AINone
import com.example.reversigame.model.ai.OseroAI

class GamePresenter{

    private val game: Game = Game()
    //private val modo2 : Modo2 = Modo2()
    private lateinit var ai: OseroAI
    private var view: GameView? = null
    val boardSize = game.BOARD_SIZE
    companion object {
        // static count
        var count: Int = 0;
    }

    var currentPlayer = Stone.BLACK

    fun onCreate(view: GameView, ai: OseroAI = AINone()) {
        this.view = view
        this.ai = ai
        view.setCurrentPlayerText(Stone.BLACK)
        game.getInitialPlaces().forEach { putStone(it) }
        view.markCanPutPlaces(game.getAllCanPutPlaces(currentPlayer))

    }

    fun onClickPlace(x: Int, y: Int) {
        if(Modo2Escolha.player == 1){
            currentPlayer = Stone.BLACK
        }else if(Modo2Escolha.player == 2){
            currentPlayer = Stone.WHITE
        }
        val clickPlace = Place(x, y, currentPlayer)
        val view = view ?: return multiplayerMode(clickPlace)
        if (!game.canPut(clickPlace)) {
            setCoord(x,y, count)
            count++
            putExchange(currentPlayer,clickPlace)
            return
        }
        view.clearAllMarkPlaces()
        putStone(clickPlace)
        if(Modo2Escolha.player == 0){
            game.getCanChangePlaces(clickPlace).forEach { putStone(it) }
        }else{
            game.getCanChangePlaces(clickPlace).forEach { multiplayerMode(it) }
        }
        putBomb(currentPlayer,clickPlace)
        if (game.isGameOver() && Modo2Escolha.player == 0) {
            val blackCount = game.countStones(Stone.BLACK)
            val whiteCount = game.countStones(Stone.WHITE)
            view.showWinner(if (blackCount > whiteCount) Stone.BLACK else Stone.WHITE, blackCount, whiteCount)
            view.finishGame()
        }
        if(Modo2Escolha.player == 0){
            changePlayer()
            view.markCanPutPlaces(game.getAllCanPutPlaces(currentPlayer))
        }
        if (!game.canNext(currentPlayer)) {
            view.clearAllMarkPlaces()
            if(Modo2Escolha.player == 0){
                changePlayer()
                view.markCanPutPlaces(game.getAllCanPutPlaces(currentPlayer))
            }
            return
        }
        // AI
        if (ai !is AINone && currentPlayer == Stone.WHITE) {
            val choseByAI = ai.computeNext(game, currentPlayer)
            onClickPlace(choseByAI.x, choseByAI.y)
        }
    }

    fun multiplayerMode(place: Place){
        Log.i("Info","Entrei no MultiplayerMode~Pretas: ${game.countStones(Stone.BLACK)} | Brancas: ${game.countStones(Stone.WHITE)}")
        if(Modo2Escolha.player == 1){
            if(Modo2.placeList[place.x][place.y].getTag() != R.drawable.white_stone){
                Modo2.placeList[place.x][place.y].setImageResource(R.drawable.black_stone)
            }
        }else if(Modo2Escolha.player == 2){
            if(Modo2.placeList[place.x][place.y].getTag() != R.drawable.black_stone){
                Modo2.placeList[place.x][place.y].setImageResource(R.drawable.white_stone)
            }
        }
        game.boardStatus[place.x][place.y].stone = place.stone
        game.getAllCanPutPlaces(currentPlayer)

    }

    @VisibleForTesting
    fun changePlayer() {
        currentPlayer = currentPlayer.other()
        view?.setCurrentPlayerText(currentPlayer)
    }

    @VisibleForTesting
    fun putStone(place: Place) {
        game.boardStatus[place.x][place.y].stone = place.stone
        view?.putStone(place)
    }

    @VisibleForTesting
    fun putBomb(player: Stone,place: Place){
        view?.putBomb(player,place)
    }

    @VisibleForTesting
    fun setCoord(x :  Int, y : Int, count : Int){
        view?.setCoord(x,y,count)
    }

    @VisibleForTesting
    fun putExchange(player: Stone,place: Place){
        view?.putExchange(player,place)
    }
}