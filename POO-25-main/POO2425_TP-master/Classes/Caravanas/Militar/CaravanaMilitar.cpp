//
// Created by mauro on 12/16/2024.
//

#include "CaravanaMilitar.h"

#include <iostream>
#include <cstdlib> // Para rand() e srand()
#include <ctime>   // Para time()

#include "../Barbara/CaravanaBarbara.h"

void CaravanaMilitar::comportamentoAutonomo(
        const std::vector<std::shared_ptr<Itens>> &itensProximos,
        const std::vector<std::shared_ptr<Caravana>> &caravanasProximas
) {
    // Perseguir bárbaros
    for (const auto &caravana : caravanasProximas) {
        if (dynamic_pointer_cast<CaravanaBarbara>(caravana)) {
            linha = caravana->getLinha();
            coluna = caravana->getColuna();
            return;
        }
    }

    // Fica parada se não houver bárbaros
}

void CaravanaMilitar::comportamentoSemTripulacao() {
    turnosSemTripulantes++;
    if (turnosSemTripulantes >= 7) {
        return;
    }

    static const int deslocX[] = {0, 1, -1, 0}; // Exemplo: Norte, Sul, Leste, Oeste
    static const int deslocY[] = {1, 0, 0, -1};

    linha = (linha + deslocX[getColuna()] + 100) % 100;
    coluna = (coluna + deslocY[getLinha()] + 100) % 100;
}

bool CaravanaMilitar::afetarPorTempestade()  {
    int chance = rand() % 101; // Número entre 0 e 7
    perderTripulantes(tripulantes*0.1);  // Perde 10% por tempestade
    if (estaSemTripulantes() || chance<=33) {
        std::cout << "Caravana "<< id << " destruída pela tempestade!" << std::endl;
        return true; // Indica que a comandoCaravana foi destruída
    }
    return false;
}
void CaravanaMilitar::perderTripulantes(int quantidade) {
    tripulantes -= quantidade;
    if (tripulantes < 0) tripulantes = 0;
}

bool CaravanaMilitar::verificarInimigo(const shared_ptr<Caravana> &outraCaravana) {
    return dynamic_pointer_cast<CaravanaBarbara>(outraCaravana) != nullptr;
}
