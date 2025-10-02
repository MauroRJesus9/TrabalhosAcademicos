package com.example.reversigame.game

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reversigame.MainActivity
import com.example.reversigame.Perfil
import com.example.reversigame.R
import com.example.reversigame.model.Place
import com.example.reversigame.model.Stone
import com.example.reversigame.model.ai.AINone
import com.example.reversigame.model.ai.OseroAI
import com.example.reversigame.val_s
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

const val SERVER_MODE = 0
const val CLIENT_MODE = 1

class Modo2 : AppCompatActivity(), GameView{
    private val model: GameViewModel by viewModels()
    companion object {
        lateinit var placeList: List<List<ImageView>>

        val EXTRA_NAME_AI = "extra_ai"

        fun createIntent(context: Context, ai: OseroAI  = AINone()): Intent {
            val intent = Intent(context, Modo1::class.java)
            intent.putExtra(EXTRA_NAME_AI, ai)
            return intent
        }
    }
    val presenter = GamePresenter()
    val boardSize = presenter.boardSize
    lateinit var nomejog1 : TextView
    lateinit var fotojog1 : de.hdodenhof.circleimageview.CircleImageView
    private var dlg: AlertDialog? = null

    var x1 = 0
    var y1 = 0
    var x2 = 0
    var y2 = 0
    var x3 = 0
    var y3 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo2)

        placeList = arrayOfNulls<List<ImageView>>(boardSize)
            .mapIndexed { x, list ->
                arrayOfNulls<ImageView>(boardSize).mapIndexed { y, imageView ->
                    val place = layoutInflater.inflate(R.layout.grid_place, null)
                    place.setOnClickListener {
                        presenter.onClickPlace(x, y)
                        Log.i("Info:","Player: ${Modo2Escolha.player}")
                        model.changeMyMove(x, y)
                    }
                    val grid = findViewById<GridLayout>(R.id.gamePlacesGrid)
                    grid.addView(place)
                    place.findViewById(R.id.gamePlaceImageView) as ImageView
                }
            }

        val ai = intent.getSerializableExtra(Modo1.EXTRA_NAME_AI) as? OseroAI ?: AINone()
        presenter.onCreate(this, ai)
        nomejog1 = findViewById(R.id.nomejog1)
        fotojog1 = findViewById(R.id.fotojog1)
        if (Perfil.nomestr != "") {
            nomejog1.text = Perfil.nomestr
        }
        if (Perfil.imgdata != null) {
            fotojog1.setImageURI(Perfil.imgdata)
        }

        model.connectionState.observe(this) { state ->
            if (state != GameViewModel.ConnectionState.SETTING_PARAMETERS &&
                state != GameViewModel.ConnectionState.SERVER_CONNECTING &&
                dlg?.isShowing == true
            ) {
                dlg?.dismiss()
                dlg = null
            }

            if (state == GameViewModel.ConnectionState.CONNECTION_ERROR) {
                finish()
            }
            if (state == GameViewModel.ConnectionState.CONNECTION_ENDED)
                finish()
        }

        if (model.connectionState.value != GameViewModel.ConnectionState.CONNECTION_ESTABLISHED) {
            when (intent.getIntExtra("mode", SERVER_MODE)) {
                SERVER_MODE -> startAsServer()
                CLIENT_MODE -> startAsClient()
            }
        }
        val db = Firebase.firestore
        val v = db.collection("Scores").document("Level2")
        v.get(Source.SERVER)
            .addOnSuccessListener {
                v.update("nrgames",it.getLong("nrgames")!!+1)
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //to do: should ask if the user wants to finish
        model.stopGame()
    }

    override fun putStone(place: Place) {
        val imageRes = when (place.stone) {
            Stone.BLACK -> R.drawable.black_stone
            Stone.WHITE -> R.drawable.white_stone
            Stone.NONE -> throw IllegalArgumentException()
        }
        placeList[place.x][place.y].setImageResource(imageRes)
    }

    override fun putBomb(player: Stone, place: Place){
        val cbp1 = findViewById<CheckBox>(R.id.peca_bombap1)
        val cbp2 = findViewById<CheckBox>(R.id.peca_bombap2)

        if(cbp1.isChecked && player == Stone.BLACK){
            //Selecionado
            placeList[place.x][place.y].setImageResource(R.color.orange)
            //Diagonal /
            placeList[place.x+1][place.y-1].setImageResource(R.color.orange)
            placeList[place.x-1][place.y+1].setImageResource(R.color.orange)

            placeList[place.x][place.y+1].setImageResource(R.color.orange)
            placeList[place.x+1][place.y].setImageResource(R.color.orange)

            placeList[place.x][place.y-1].setImageResource(R.color.orange)
            placeList[place.x-1][place.y].setImageResource(R.color.orange)
            //Diagonal \
            placeList[place.x-1][place.y-1].setImageResource(R.color.orange)
            placeList[place.x+1][place.y+1].setImageResource(R.color.orange)
            cbp1.isEnabled = false
            cbp1.isChecked = false
        }
        if(cbp2.isChecked && player == Stone.WHITE){
            placeList[place.x][place.y].setImageResource(R.color.orange)
            //Diagonal /
            placeList[place.x+1][place.y-1].setImageResource(R.color.orange)
            placeList[place.x-1][place.y+1].setImageResource(R.color.orange)

            placeList[place.x][place.y+1].setImageResource(R.color.orange)
            placeList[place.x+1][place.y].setImageResource(R.color.orange)

            placeList[place.x][place.y-1].setImageResource(R.color.orange)
            placeList[place.x-1][place.y].setImageResource(R.color.orange)
            //Diagonal \
            placeList[place.x-1][place.y-1].setImageResource(R.color.orange)
            placeList[place.x+1][place.y+1].setImageResource(R.color.orange)
            cbp2.isEnabled = false
            cbp2.isChecked = false
        }
    }

    override fun setCoord(x :  Int, y : Int, count : Int){
        if(count == 0){
            x1 = x;
            y1 = y;
        }else if(count == 1){
            x2 = x;
            y2 = x;
        }else if(count == 2){
            x3 = x;
            y3 = y;
        }
    }

    override fun putExchange(player: Stone, place: Place) {
        val cbp1 = findViewById<CheckBox>(R.id.troca_pecasp1)
        val cbp2 = findViewById<CheckBox>(R.id.troca_pecasp2)

        if(GamePresenter.count >= 3 && cbp1.isChecked && player ==  Stone.BLACK){
            placeList[x1][y1].setImageResource(R.drawable.white_stone)
            placeList[x2][y2].setImageResource(R.drawable.white_stone)
            placeList[x3][y3].setImageResource(R.drawable.black_stone)

            cbp1.isEnabled = false
            cbp1.isChecked = false
            GamePresenter.count = 0
        }
        if(GamePresenter.count >= 3 && cbp2.isChecked && player ==  Stone.WHITE){
            placeList[x1][y1].setImageResource(R.drawable.black_stone)
            placeList[x2][y2].setImageResource(R.drawable.black_stone)
            placeList[x3][y3].setImageResource(R.drawable.white_stone)
            cbp2.isEnabled = false
            cbp2.isChecked = false
            GamePresenter.count = 0
        }
        //Toast.makeText(applicationContext, "C:${GamePresenter.count}",Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("StringFormatInvalid")
    override fun setCurrentPlayerText(player: Stone) {
        var player1 : String;
        if(Perfil.nomestr == ""){
            player1 = getString(R.string.player1)
        }else{
            player1 = Perfil.nomestr
        }
        val color = when (player) {
            Stone.BLACK -> player1
            Stone.WHITE -> getString(R.string.player2)
            Stone.NONE -> throw IllegalArgumentException()
        }
        if(color != getString(R.string.player2)){
            val player = findViewById<TextView>(R.id.gameCurrentPlayerText)
            player.text = getString(R.string.current_player, color)
        }
    }

    override fun showWinner(player: Stone, blackCount: Int, whiteCount: Int) {
        var player1 : String;
        if(Perfil.nomestr == ""){
            player1 = getString(R.string.player1)
        }else{
            player1 = Perfil.nomestr
        }
        val color = when (player) {
            Stone.BLACK -> player1
            Stone.WHITE -> getString(R.string.player2)
            Stone.NONE -> throw IllegalArgumentException()
        }
        Toast.makeText(this, getString(R.string.winnerName,color), Toast.LENGTH_SHORT).show()
    }

    override fun finishGame() {
        finish()
    }

    override fun markCanPutPlaces(places: List<Place>) {
        places.forEach { placeList[it.x][it.y].setBackgroundColor(ContextCompat.getColor(this, R.color.black)) }
    }

    override fun clearAllMarkPlaces() {
        placeList.flatMap { it }.forEach { it.setBackgroundColor(
            ContextCompat.getColor(this,
            R.color.orange
        )) }
    }

    private fun startAsServer() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )
        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
            })
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = String.format(getString(R.string.msg_ip_address),strIPAddress)
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }
        dlg = AlertDialog.Builder(this)
            .setTitle(getString(R.string.server_mode))
            .setView(ll)
            .setOnCancelListener {
                model.stopServer()
                finish()
            }
            .create()
        model.startServer()
        dlg?.show()
    }

    private fun startAsClient() {
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    source?.run {
                        var ret = ""
                        forEach {
                            if (it.isDigit() || it.equals('.'))
                                ret += it
                        }
                        return ret
                    }
                    return null
                }

            })
        }
        val dlg = AlertDialog.Builder(this)
            .setTitle(getString(R.string.client_mode))
            .setMessage(getString(R.string.ask_ip))
            .setPositiveButton(getString(R.string.button_connect)) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(this, getString(R.string.error_address), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    model.startClient(strIP)
                }
            }
            .setNeutralButton(getString(R.string.btn_emulator)) { _: DialogInterface, _: Int ->
                model.startClient("10.0.2.2", SERVER_PORT-1)
                // Configure port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
            .setNegativeButton(getString(R.string.button_cancel)) { _: DialogInterface, _: Int ->
                finish()
            }
            .setCancelable(false)
            .setView(edtBox)
            .create()

        dlg.show()
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
                val myIntent = Intent(applicationContext, Modo2::class.java)
                startActivityForResult(myIntent, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}