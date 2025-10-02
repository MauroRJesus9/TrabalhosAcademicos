package com.example.reversigame.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.reversigame.Perfil
import com.example.reversigame.R
import com.example.reversigame.model.ai.AINone

class Modo2Escolha : AppCompatActivity(){

    private lateinit var srvButton: Button
    private lateinit var cltButton: Button

    companion object {
        var player = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo2escolha)

        srvButton = findViewById(R.id.btnModoServ)
        srvButton.setOnClickListener {
            Perfil.nomestr = "Servidor"
            player = 1
            Log.i("Info:","Servidor")
            startGame(SERVER_MODE)
        }
        cltButton = findViewById(R.id.btnModoCli)
        cltButton.setOnClickListener {
            Perfil.nomestr = "Cliente"
            player = 2
            Log.i("Info:","Cliente")
            startGame(CLIENT_MODE)
        }
    }

    fun startGame(mode : Int) {
        //val intent = Modo2.createIntent(this, AINone()).putExtra("mode",mode)
        val intent = Intent(this,Modo2::class.java).apply {
            putExtra("mode",mode)
        }
        startActivity(intent)
    }
}