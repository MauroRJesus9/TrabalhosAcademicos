package com.example.reversigame.game

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reversigame.model.Stone
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

const val SERVER_PORT = 9999

class GameViewModel : ViewModel(){
    enum class State {
        STARTING, PLAYING_BOTH, PLAYING_ME, PLAYING_OTHER, ROUND_ENDED, GAME_OVER
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    val state = MutableLiveData(State.STARTING)
    val connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    var myMoveX = 0
    var myMoveY = 0
    var otherMoveX = 0
    var otherMoveY = 0

    val presenter = GamePresenter()
    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    private var serverSocket: ServerSocket? = null

    private var threadComm: Thread? = null

    fun startGame() {
        myMoveX = 0
        myMoveY = 0
        otherMoveX = 0
        otherMoveY = 0
        state.postValue(State.PLAYING_ME)
    }

    fun changeMyMove(moveX: Int, moveY : Int) {
        if (connectionState.value != ConnectionState.CONNECTION_ESTABLISHED)
            return
        Log.i("Client:","X:$moveX | Y:$moveY")
        Log.i("Server Move:","X: $otherMoveX | Y: $otherMoveY")

        presenter.onClickPlace(moveX,moveY)

        if(Modo2Escolha.player == 1 && otherMoveX != 0 && otherMoveY != 0){
            Modo2Escolha.player = 2;
            presenter.onClickPlace(otherMoveX,otherMoveY)
            Modo2Escolha.player = 1 //Volta ao servidor
        }else if(Modo2Escolha.player == 2 && otherMoveX != 0 && otherMoveY != 0){
            Modo2Escolha.player = 1 //Volta ao servidor
            presenter.onClickPlace(otherMoveX,otherMoveY)
            Modo2Escolha.player = 2
        }

        myMoveX = moveX
        myMoveY = moveY

        /*if(otherMoveX != 0 && otherMoveY != 0){
            presenter.onClickPlace(otherMoveX,otherMoveY)
        }*/

        socketO?.run {
            thread {
                try {
                    val printStream = PrintStream(this)
                    printStream.println("$moveX $moveY")
                    printStream.flush()
                } catch (_: Exception) {
                    stopGame()
                }
            }
        }
        state.postValue(State.PLAYING_OTHER)
        //checkIfSomeoneWins()
    }
    private fun changeOtherMove(moveX: Int, moveY : Int) {
        //presenter.onClickPlace(moveX,moveY)
        Log.i("Player", "${Modo2Escolha.player}")
        Log.i("Server:","X:$moveX | Y:$moveY")
        Log.i("Client Move:","X:$myMoveX | Y:$myMoveY")
        //presenter.multiplayerMode(moveX,moveY)

        if(Modo2Escolha.player == 1){
            Modo2Escolha.player = 2;
            presenter.onClickPlace(moveX,moveY)
            Modo2Escolha.player = 1 //Volta ao servidor
        }else if(Modo2Escolha.player == 2){
            Modo2Escolha.player = 1 //Volta ao servidor
            presenter.onClickPlace(moveX,moveY)
            Modo2Escolha.player = 2
        }

        otherMoveX = moveX
        otherMoveY = moveY
        state.postValue(State.PLAYING_ME)
        //checkIfSomeoneWins()
    }



    fun startServer() {
        if (serverSocket != null || socket != null ||
            connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return

        connectionState.postValue(ConnectionState.SERVER_CONNECTING)

        thread {
            serverSocket = ServerSocket(SERVER_PORT)
            serverSocket?.run {
                try {
                    val socketClient = serverSocket!!.accept()
                    startComm(socketClient)
                } catch (_: Exception) {
                    connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                } finally {
                    serverSocket?.close()
                    serverSocket = null
                }
            }
        }
    }

    fun stopServer() {
        serverSocket?.close()
        connectionState.postValue(ConnectionState.CONNECTION_ENDED)
        serverSocket = null
    }

    fun startClient(serverIP: String,serverPort: Int = SERVER_PORT) {
        if (socket != null || connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return

        thread {
            connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
            try {
                //val newsocket = Socket(serverIP, serverPort)
                val newsocket = Socket()
                newsocket.connect(InetSocketAddress(serverIP,serverPort),5000)
                startComm(newsocket)
            } catch (_: Exception) {
                connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                stopGame()
            }
        }
    }

    private fun startComm(newSocket: Socket) {
        if (threadComm != null)
            return

        socket = newSocket

        threadComm = thread {
            try {
                if (socketI == null)
                    return@thread

                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                val bufI = socketI!!.bufferedReader()

                while (state.value != State.GAME_OVER) {
                    val message = bufI.readLine()  //Mensagem com a posicao
                    val splitStr: List<String> = message.split(" ")
                    val moveX = splitStr[0].toInt()
                    val moveY = splitStr[1].toInt()
                    changeOtherMove(moveX,moveY)
                }
            } catch (_: Exception) {
            } finally {
                stopGame()
            }
        }
    }

    fun stopGame() {
        try {
            state.postValue(State.GAME_OVER)
            connectionState.postValue(ConnectionState.CONNECTION_ERROR)
            socket?.close()
            socket = null
            threadComm?.interrupt()
            threadComm = null
        } catch (_: Exception) { }
    }


}