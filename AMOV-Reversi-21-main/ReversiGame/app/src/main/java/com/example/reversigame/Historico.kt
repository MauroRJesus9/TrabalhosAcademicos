package com.example.reversigame

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.reversigame.databinding.ActivityHistoricoBinding
import java.util.*

class Historico : AppCompatActivity() {
    lateinit var b : ActivityHistoricoBinding
    lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHistoricoBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.lvHistorico.adapter = HistoricoAdapter()


    }

    class HistoricoAdapter() : BaseAdapter(){
        override fun getCount(): Int {
            return HistoricoPointer.lista.size
        }

        override fun getItem(p0: Int): Any {
            return HistoricoPointer.lista[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val view = LayoutInflater.from(p2!!.context).inflate(R.layout.item_historico,p2,false)
            val historico = HistoricoPointer.lista[p0]
            view.findViewById<TextView>(R.id.tvInfoMode).text = historico.modo
            view.findViewById<TextView>(R.id.tvInfoPlayers).text = ("Vencedor: " + historico.jogador_vencedor)
            Log.i("Info:","${historico.preview}")
            view.findViewById<ImageView>(R.id.imgPreview).setImageBitmap(historico.preview?.let {
                Bitmap.createScaledBitmap(
                    it,120,120,false)
            })
            view.findViewById<TextView>(R.id.totalPecas).text = ("Pretas: "+historico.black+" Brancas: "+historico.white)
            return view
        }

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
                val myIntent = Intent(applicationContext, Historico::class.java)
                startActivityForResult(myIntent, 0)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}