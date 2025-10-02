//
// Created by mauro on 12/16/2024.
//

#include "CaravanaComercio.h"

#include <cstdlib> // Para rand() e srand()
#include <ctime>   // Para time()
#include <iostream>

#include "../Barbara/CaravanaBarbara.h"

void CaravanaComercio::comportamentoAutonomo(
        const std::vector<std::shared_ptr<Itens>> &itensProximos,
        const std::vector<std::shared_ptr<Caravana>> &caravanasProximas
) {
    // Verificar itens próximos
    if (!itensProximos.empty()) {
        auto item = itensProximos[0];
        linha = item->getLinha();
        coluna = item->getColuna();
        return;
    }

    // Movimento aleatório genérico caso não haja itens
    Caravana::comportamentoGeralAutonomo();
}

void CaravanaComercio::comportamentoSemTripulacao() {
    turnosSemTripulantes++;
    if (turnosSemTripulantes >= 5) {
        return;
    }

    // Movimento aleatório
    static const int deslocX[] = {0, 1, -1, 0};
    static const int deslocY[] = {1, 0, 0, -1};

    int direcao = rand() % 4;
    linha = (linha + deslocX[direcao] + 100) % 100;
    coluna = (coluna + deslocY[direcao] + 100) % 100;
}

bool CaravanaComercio::afetarPorTempestade() {
    if (rand() % 100 < (cargaAtual > capacidadeCarga / 2 ? 50 : 25)) {
        return true;
    } else {
        cargaAtual *= 0.75;
    }
    return false;
}

bool CaravanaComercio::verificarInimigo(const std::shared_ptr<Caravana>& outraCaravana) {
    return dynamic_pointer_cast<CaravanaBarbara>(outraCaravana) != nullptr;
}
