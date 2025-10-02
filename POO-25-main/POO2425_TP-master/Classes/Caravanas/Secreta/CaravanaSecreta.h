//
// Created by mauro on 12/16/2024.
//

#ifndef SECRETA_H
#define SECRETA_H
#include "../Caravana.h"


class CaravanaSecreta : public Caravana {//Caravana de batedores, leve e rápida útil para afastar bárbaros
    static const int MAX_TRIPULACAO = 2;
    static const int MAX_CARGA = 1;
    static const int MAX_AGUA = 20;

public:
    explicit CaravanaSecreta(int id,int linha, int coluna) : Caravana(id,linha,coluna) {
        deslocacaoPturno = 1;
        tripulantes=MAX_TRIPULACAO;
        capacidadeCarga=MAX_CARGA;
        capacidadeAgua=MAX_AGUA;
        aguaAtual = capacidadeAgua;
        gastAgua=1;//gasta 1 por instante, 0 se tiver menos de metade da tripulacao e 0 se nao tiver tripulacao
    }
    char getTipo() const override { return '?'; }
    bool afetarPorTempestade() override;
    void perderTripulantes(int quantidade);
    void comportamentoAutonomo(const vector<std::shared_ptr<Itens>> &itensProximos,const vector<std::shared_ptr<Caravana>> &caravanasProximas) override;
    void beberAgua() override {aguaAtual-=gastAgua;}
    bool verificarInimigo(const shared_ptr<Caravana> &outraCaravana) override;
    void comportamentoSemTripulacao() override;
};



#endif //SECRETA_H
