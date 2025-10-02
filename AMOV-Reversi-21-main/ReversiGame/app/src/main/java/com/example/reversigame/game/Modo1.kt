package com.example.reversigame.game

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reversigame.*
import com.example.reversigame.model.Place
import com.example.reversigame.model.Stone
import com.example.reversigame.model.ai.AINone
import com.example.reversigame.model.ai.OseroAI
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import android.graphics.drawable.BitmapDrawable




class Modo1 : AppCompatActivity(), GameView{

    lateinit var placeList: List<List<ImageView>>
    lateinit var nomejog1 : TextView
    lateinit var fotojog1 : de.hdodenhof.circleimageview.CircleImageView
    lateinit var grid : GridLayout
    val presenter = GamePresenter()
    val boardSize = presenter.boardSize
    lateinit var historico : HistoricoLista

    var x1 = 0
    var y1 = 0
    var x2 = 0
    var y2 = 0
    var x3 = 0
    var y3 = 0

    //Atribui player1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo1)
        Modo2Escolha.player = 0

        placeList = arrayOfNulls<List<ImageView>>(boardSize)
            .mapIndexed { x, list ->
                arrayOfNulls<ImageView>(boardSize).mapIndexed { y, imageView ->
                    val place = layoutInflater.inflate(R.layout.grid_place, null)
                    place.setOnClickListener { presenter.onClickPlace(x, y) }
                    val grid = findViewById<GridLayout>(R.id.gamePlacesGrid)
                    grid.addView(place)
                    place.findViewById(R.id.gamePlaceImageView) as ImageView
                }
            }

        val ai = intent.getSerializableExtra(EXTRA_NAME_AI) as? OseroAI ?: AINone()
        presenter.onCreate(this, ai)
        nomejog1 = findViewById(R.id.nomejog1)
        fotojog1 = findViewById(R.id.fotojog1)
        grid = findViewById(R.id.gamePlacesGrid)
        if(Perfil.nomestr != ""){
            nomejog1.text = Perfil.nomestr
        }
        if(Perfil.imgdata != null){
            Log.i("Info",Perfil.imgdata.toString())
            fotojog1.setImageURI(null)
            fotojog1.setImageURI(Perfil.imgdata)
        }
        val db = Firebase.firestore
        val v = db.collection("Scores").document("Level1")
        v.get(Source.SERVER)
            .addOnSuccessListener {
                v.update("nrgames",it.getLong("nrgames")!!+1)
            }

    }

    fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(
            v.layoutParams.width,
            v.layoutParams.height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }


    override fun putStone(place: Place) {
        val imageRes = when (place.stone) {
            Stone.BLACK -> R.drawable.black_stone
            Stone.WHITE -> R.drawable.white_stone
            Stone.NONE -> throw IllegalArgumentException()
        }
        placeList[place.x][place.y].setImageResource(imageRes)
    }

    override fun putBomb(player: Stone,place: Place){
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
        val player = findViewById<TextView>(R.id.gameCurrentPlayerText)
        player.text = getString(R.string.current_player, color)
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
        if(color == player1){
            val db = Firebase.firestore
            val v = db.collection("Scores").document("Level1")
            v.get(Source.SERVER)
                .addOnSuccessListener {
                    v.update("nrwins",it.getLong("nrwins")!!+1)
                }
        }
        //Adiciona ao historico
        addHistorico(color,blackCount,whiteCount)
        //HistoricoPointer.lista[Historico.countHistorico].preview = loadBitmapFromView(View(this)

        Toast.makeText(this, getString(R.string.winnerName,color), Toast.LENGTH_SHORT).show()
    }

    fun addHistorico(player : String, blackCount: Int, whiteCount: Int){
        val bm = (fotojog1.getDrawable() as BitmapDrawable).bitmap
        historico = HistoricoLista("Modo 1",player,bm,blackCount,whiteCount)
        Log.i("Info----->", "${loadBitmapFromView(findViewById(R.id.fotojog1))}")
        HistoricoPointer.lista.add(historico)
    }

    override fun finishGame() {
        finish()
    }

    override fun markCanPutPlaces(places: List<Place>) {
        places.forEach { placeList[it.x][it.y].setBackgroundColor(ContextCompat.getColor(this, R.color.black)) }
    }

    override fun clearAllMarkPlaces() {
        placeList.flatMap { it }.forEach { it.setBackgroundColor(ContextCompat.getColor(this,
            R.color.orange
        )) }
    }

    companion object {
        val EXTRA_NAME_AI = "extra_ai"

        fun createIntent(context: Context, ai: OseroAI  = AINone()): Intent {
            val intent = Intent(context, Modo1::class.java)
            intent.putExtra(EXTRA_NAME_AI, ai)
            return intent
        }
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
                val myIntent = Intent(applicationContext, Modo1::class.java)
                startActivityForResult(myIntent, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}