//
// Created by mauro on 12/16/2024.
//

#include "Itens.h"
#include <iostream>

Itens::Itens(TipoItem tipo, int tempoRestante, int linha, int coluna)
        : tipo(tipo), tempoRestante(tempoRestante), linha(linha), coluna(coluna) {}

void Itens::reduzirTempo() {
    if (tempoRestante > 0) tempoRestante--;
}

std::string Itens::getDescricao() const {
    switch (tipo) {
        case TipoItem::CaixaPandora: return "Caixa de Pandora";
        case TipoItem::ArcaTesouro: return "Arca do Tesouro";
        case TipoItem::Jaula: return "Jaula";
        case TipoItem::Mina: return "Mina";
        case TipoItem::Surpresa: return "Surpresa";
        default: return "Desconhecido";
    }
}