//
// Created by mauro on 12/16/2024.
//

#ifndef BARBARA_H
#define BARBARA_H
#include "../Caravana.h"

class CaravanaBarbara : public Caravana {
    static const int MAX_TRIPULACAO = 40;
    static const int MAX_CARGA = 1;
    static const int MAX_AGUA = 1;
    int countTurnos;

public:
    explicit CaravanaBarbara(int id, int linha, int coluna) : Caravana(id,linha,coluna) {
        deslocacaoPturno = 1;
        tripulantes=MAX_TRIPULACAO;
        capacidadeCarga=MAX_CARGA;
        capacidadeAgua=MAX_AGUA;
        gastAgua=0;//gasta 0 agua
        aguaAtual = capacidadeAgua;
        countTurnos=0;
        isAutonomo=true;
    }
    char getTipo() const override { return '!'; }
    bool afetarPorTempestade() override;
    void beberAgua() override;
    void perderTripulantes(int quantidade);
    bool verificarInimigo(const shared_ptr<Caravana> &outraCaravana) override;
    void comportamentoAutonomo(const vector<std::shared_ptr<Itens>> &itensProximos,
                               const vector<std::shared_ptr<Caravana>> &caravanasProximas) override;
    void comportamentoSemTripulacao() override;
};



#endif //BARBARA_H
