package com.example.reversigame

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.net.Uri
import android.os.PersistableBundle
import android.util.Log
import com.example.reversigame.authentication.Auth
import com.example.reversigame.game.GamePresenter
import com.example.reversigame.game.Modo1
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_modo1.*
import kotlinx.android.synthetic.main.activity_perfil.*
import org.w3c.dom.Text
import java.io.File



class Perfil : AppCompatActivity(){
    lateinit var imageView: ImageView
    lateinit var button: Button
    lateinit var buttonLogin : Button
    lateinit var editarperfil : Button
    lateinit var playedMatches1 : TextView
    lateinit var playedMatches2 : TextView

    private val pickImage = 100
    companion object {
        var nomestr: String = ""
        var emailstr : String = ""
        var imgdata : Uri? = null
        var nrgames : Long = 0
        var nrwins : Long = 0
        var nrgames2 : Long = 0
        var nrwins2 : Long = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val db = Firebase.firestore
        db.collection("Scores").document("Level1")
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    return@addSnapshotListener
                }
                if (docSS!=null && docSS.exists()) {
                    nrgames = docSS.getLong("nrgames")!!
                    nrwins = docSS.getLong("nrwins")!!
                    Log.i("Info:","Jogos: $nrgames | Vitorias: $nrwins")
                }
            }
        db.collection("Scores").document("Level2")
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    return@addSnapshotListener
                }
                if (docSS!=null && docSS.exists()) {
                    nrgames2 = docSS.getLong("nrgames")!!
                    nrwins2 = docSS.getLong("nrwins")!!
                    Log.i("Info:","Jogos: $nrgames2 | Vitorias: $nrwins2")
                }
            }

        imageView = findViewById(R.id.profilephoto)
        button = findViewById(R.id.choosephoto)
        button.setOnClickListener {
            //val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            //startActivityForResult(gallery, pickImage)
            val intent = Intent(this,Camera::class.java)
            startActivity(intent)
        }

        buttonLogin = findViewById(R.id.realizaLogin)
        buttonLogin.setOnClickListener {
            val intent = Intent(this,Auth::class.java)
            startActivity(intent)
        }


        editarperfil = findViewById(R.id.editprofile)
        val nometv = findViewById<TextView>(R.id.nometv)
        val emailtv = findViewById<TextView>(R.id.emailtv)
        val nome = findViewById<EditText>(R.id.nome_jog)
        val email = findViewById<EditText>(R.id.email_jog)
        if(imgdata != null){
            nome.visibility = View.GONE
            email.visibility = View.GONE
            nometv.text = getString(R.string.name, nomestr)
            emailtv.text = getString(R.string.email,emailstr)
            imageView.setImageURI(imgdata)
        }else{
            editarperfil.setOnClickListener {


                nomestr = nome.text.toString()
                emailstr = email.text.toString()

                if(nomestr != "" && emailstr != ""){
                    nometv.text = getString(R.string.name, nomestr)
                    emailtv.text = getString(R.string.email,emailstr)

                    nome.visibility = View.GONE
                    email.visibility = View.GONE
                }
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery, pickImage)
            }
        }

        playedMatches1 = findViewById(R.id.mode1)
        playedMatches1.text = getString(R.string.mode1, nrgames.toString(), nrwins.toString())
        playedMatches2 = findViewById(R.id.mode2)
        playedMatches2.text = getString(R.string.mode2, nrgames2.toString(), nrwins2.toString())

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
                val myIntent = Intent(applicationContext, Perfil::class.java)
                startActivityForResult(myIntent, 0)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageView.setImageURI(data?.data)
        }

        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                val nome = intent.getStringExtra("nome")
                val email = intent.getStringExtra("email")

                val nomep = findViewById<TextView>(R.id.nome_jog)
                nomep.text = getString(R.string.name, nome.toString())
                val emailp = findViewById<TextView>(R.id.email_jog)
                emailp.text = getString(R.string.email,email.toString())
            }
        }
    }

}