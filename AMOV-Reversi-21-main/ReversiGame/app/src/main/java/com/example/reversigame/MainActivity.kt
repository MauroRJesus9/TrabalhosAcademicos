package com.example.reversigame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import android.content.res.Resources

import java.util.Locale

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


var val_s = "pt"
//SHA1: 05:34:37:FC:70:36:0C:7F:6B:6C:7A:4A:7A:05:2C:79:A3:61:B3:51


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Firebase.firestore
        db.collection("Scores").document("Level1")
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    return@addSnapshotListener
                }
                if (docSS!=null && docSS.exists()) {
                    Perfil.nrgames = docSS.getLong("nrgames")!!
                    Perfil.nrwins = docSS.getLong("nrwins")!!
                    Log.i("Info:","Jogos: ${Perfil.nrgames} | Vitorias: ${Perfil.nrwins}")
                }
            }
        db.collection("Scores").document("Level2")
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    return@addSnapshotListener
                }
                if (docSS!=null && docSS.exists()) {
                    Perfil.nrgames2 = docSS.getLong("nrgames")!!
                    Perfil.nrwins2 = docSS.getLong("nrwins")!!
                    Log.i("Info:","Jogos: ${Perfil.nrgames2} | Vitorias: ${Perfil.nrwins2}")
                }
            }
    }

    fun onPerfil(view: android.view.View) {
        val intent = Intent(this,Perfil::class.java)
        startActivity(intent)

    }

    fun onHistorico(view: android.view.View) {
        val intent = Intent(this,Historico::class.java)
        startActivity(intent)
    }

    fun onSair(view: android.view.View) {
        this.finish()
    }

    fun onJogar(view: android.view.View) {
        val intent = Intent(this,JogarMenu::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.MnCreditos -> {
                val toast = Toast.makeText(applicationContext, "Criado por: Mauro Jesus & Pedro Ramos & Rúben Almeida", Toast.LENGTH_LONG)
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
                val myIntent = Intent(applicationContext, MainActivity::class.java)
                startActivityForResult(myIntent, 0)
            }
        }
        return true
    }

}