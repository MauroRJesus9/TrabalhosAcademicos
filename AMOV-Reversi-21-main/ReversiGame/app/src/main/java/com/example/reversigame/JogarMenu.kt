package com.example.reversigame

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reversigame.game.Modo1
import com.example.reversigame.game.Modo2Escolha
import com.example.reversigame.model.ai.AINone
import java.util.*

class JogarMenu : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jogar)
    }

    fun onModo1(view: android.view.View) {
        startActivity(Modo1.createIntent(this, AINone()))
    }

    fun onModo2(view: android.view.View) {
        val intent = Intent(this,Modo2Escolha::class.java)
        startActivity(intent)
    }

    fun onModo3(view: android.view.View) {

    }

    fun onVoltar(view: android.view.View) {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.MnCreditos -> {
                val toast = Toast.makeText(this, "Criado por: Mauro Jesus & Pedro Ramos & Rúben Almeida", Toast.LENGTH_LONG)
                toast.show()
            }
            R.id.MnLinguagem -> {
                var locale = Locale(val_s)
                if(val_s == "pt"){
                    locale = Locale(val_s)
                    val_s = "en"
                }else{
                    locale = Locale(val_s)
                    val_s = "pt"
                }
                val config: Configuration = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
                val myIntent = Intent(applicationContext, JogarMenu::class.java)
                startActivityForResult(myIntent, 0)
            }
        }

        return super.onOptionsItemSelected(item)
    }

}