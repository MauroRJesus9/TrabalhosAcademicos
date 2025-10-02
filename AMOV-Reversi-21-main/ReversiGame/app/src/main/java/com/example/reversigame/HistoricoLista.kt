package com.example.reversigame

import android.graphics.Bitmap

data class HistoricoLista (
    var modo: String? = null,
    var jogador_vencedor : String? = null,
    var preview : Bitmap? = null,
    var black : Int = 0,
    var white : Int = 0
){

}