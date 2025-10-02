//
// Created by mauro on 12/16/2024.
//

#ifndef ITEM_H
#define ITEM_H

#include <string>
#include <memory>
#include "../Zonas/Zona.h"

enum class TipoItem {
    CaixaPandora,
    ArcaTesouro,
    Jaula,
    Mina,
    Surpresa
};

class Itens : public Zona {
    TipoItem tipo;
    int tempoRestante;
    int linha, coluna;
public:
    Itens(TipoItem tipo, int tempoRestante, int linha, int coluna);
    TipoItem getTipoItem() const { return tipo; }
    int getTempoRestante() const { return tempoRestante; }
    int getLinha() const { return linha; }
    int getColuna() const { return coluna; }

    void reduzirTempo();
    bool expirou() const { return tempoRestante <= 0; }
    std::string getDescricao() const;
    char getTipo() const override { return 'I'; }
};

#endif // ITEM_H
