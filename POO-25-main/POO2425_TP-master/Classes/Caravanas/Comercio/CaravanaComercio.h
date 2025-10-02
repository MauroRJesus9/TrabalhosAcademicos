//
// Created by mauro on 12/16/2024.
//

#ifndef COMERCIO_H
#define COMERCIO_H
#include "../Caravana.h"

class CaravanaComercio : public Caravana {
    static const int MAX_TRIPULACAO = 20;
    static const int MAX_CARGA = 40;
    static const int MAX_AGUA = 200;

public:
    explicit CaravanaComercio(int id, int linha, int coluna) : Caravana(id,linha,coluna) {
        deslocacaoPturno = 2;
        tripulantes=MAX_TRIPULACAO;
        capacidadeCarga=MAX_CARGA;
        capacidadeAgua=MAX_AGUA;
        aguaAtual = capacidadeAgua;
        gastAgua=2;//gasta 2 por instante, 1 se tiver menos de metade da tripulacao e 0 se nao tiver tripulacao
    }
    void beberAgua() override{aguaAtual-=gastAgua;}
    char getTipo() const override {  return '0' + id; }
    bool afetarPorTempestade() override;
    bool verificarInimigo(const shared_ptr<Caravana> &outraCaravana) override;
    void comportamentoAutonomo(const vector<std::shared_ptr<Itens>> &itensProximos,
                               const vector<std::shared_ptr<Caravana>> &caravanasProximas) override;
    void comportamentoSemTripulacao() override;
};


#endif //COMERCIO_H
