//
// Created by mauro on 12/16/2024.
//

#ifndef MILITAR_H
#define MILITAR_H
#include "../Caravana.h"

class CaravanaMilitar : public Caravana {
    static const int MAX_TRIPULACAO = 40;
    static const int MAX_CARGA = 5;
    static const int MAX_AGUA = 400;
    std::string ultimaDirecao;

public:
    explicit CaravanaMilitar(int id, int linha, int coluna) : Caravana(id,linha,coluna) {
        deslocacaoPturno = 3;
        tripulantes=MAX_TRIPULACAO;
        capacidadeCarga=MAX_CARGA;
        capacidadeAgua=MAX_AGUA;
        aguaAtual = capacidadeAgua;
        gastAgua=3;//gasta 3 por instante, 1 se tiver menos de metade da tripulacao e 1 se nao tiver tripulacao
    }
    void registrarUltimaDirecao(const std::string& direcao) {
        ultimaDirecao = direcao;
    }

    std::string getUltimaDirecao() const {
        return ultimaDirecao.empty() ? "N" : ultimaDirecao; // Direção padrão
    }

    char getTipo() const override { return 'M'; }
    bool afetarPorTempestade() override;
    void perderTripulantes(int quantidade);
    void beberAgua() override{aguaAtual-=gastAgua;}
    bool verificarInimigo(const shared_ptr<Caravana> &outraCaravana) override;
    void comportamentoAutonomo(const vector<std::shared_ptr<Itens>> &itensProximos,
                               const vector<std::shared_ptr<Caravana>> &caravanasProximas) override;
    void comportamentoSemTripulacao() override;
};

#endif //MILITAR_H

