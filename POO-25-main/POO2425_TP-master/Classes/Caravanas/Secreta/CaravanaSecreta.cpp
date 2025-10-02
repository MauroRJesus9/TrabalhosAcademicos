//
// Created by mauro on 12/16/2024.
//

#include "CaravanaSecreta.h"

#include <iostream>
#include <cstdlib> // Para rand() e srand()
#include <ctime>   // Para time()

#include "../Barbara/CaravanaBarbara.h"

bool CaravanaSecreta::afetarPorTempestade()  {
    int chance = rand() % 101; // Número entre 0 e 7
    perderTripulantes(tripulantes*0.5);  // Perde 5% por tempestade
    aguaAtual *= 0.90;
    if (estaSemTripulantes() || chance<=10) {
        std::cout << "Caravana "<< id << " destruída pela tempestade!" << std::endl;
        return true; // Indica que a comandoCaravana foi destruída
    }
    return false;
}
void CaravanaSecreta::perderTripulantes(int quantidade) {
    tripulantes -= quantidade;
    if (tripulantes < 0) tripulantes = 0;
}

void CaravanaSecreta::comportamentoSemTripulacao() {
    turnosSemTripulantes++;
    if (turnosSemTripulantes >= 6) {
        return;
    }

    // Movimento em zigue-zague
    if (turnosSemTripulantes % 2 == 0) {
        linha = (linha + 1 + 100) % 100; // Move para baixo
    } else {
        coluna = (coluna + 1 + 100) % 100; // Move para direita
    }
}

bool CaravanaSecreta::verificarInimigo(const shared_ptr<Caravana> &outraCaravana) {
    return dynamic_pointer_cast<CaravanaBarbara>(outraCaravana) != nullptr;
}

void CaravanaSecreta::comportamentoAutonomo(const vector<std::shared_ptr<Itens>> &itensProximos,
                                            const vector<std::shared_ptr<Caravana>> &caravanasProximas) {
    // Movimento aleatório se não encontrar ninguém
    comportamentoGeralAutonomo();
}

