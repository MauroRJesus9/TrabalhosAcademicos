//
// Created by mauro on 12/16/2024.
//

#include <cstdlib> // Para rand() e srand()
#include <ctime>   // Para time()
#include "CaravanaBarbara.h"

#include <iostream>

#include "../Secreta/CaravanaSecreta.h"
#include "../Militar/CaravanaMilitar.h"
#include "../Comercio/CaravanaComercio.h"

void CaravanaBarbara::comportamentoAutonomo(
        const std::vector<std::shared_ptr<Itens>> &itensProximos,
        const std::vector<std::shared_ptr<Caravana>> &caravanasProximas
) {
    // Perseguir qualquer outra caravana
    for (const auto &caravana : caravanasProximas) {
        if (!dynamic_pointer_cast<CaravanaBarbara>(caravana)) {
            linha = caravana->getLinha();
            coluna = caravana->getColuna();
            return;
        }
    }

    // Movimento aleatório se não encontrar ninguém
    Caravana::comportamentoGeralAutonomo();
}

void CaravanaBarbara::comportamentoSemTripulacao() {
    
}

void CaravanaBarbara::beberAgua() {
    aguaAtual = capacidadeAgua;
}

bool CaravanaBarbara::afetarPorTempestade()  {
    int chance = rand() % 101; // Número entre 0 e 7
    perderTripulantes(tripulantes*0.1);  // Perde 10% por tempestade
    if (estaSemTripulantes() || chance<=25) {
        std::cout << "Caravana barbara destruída pela tempestade!" << std::endl;
        return true; // Indica que a comandoCaravana foi destruída
    }
    return false;
}
void CaravanaBarbara::perderTripulantes(int quantidade) {
    tripulantes -= quantidade;
    if (tripulantes < 0) tripulantes = 0;
}
bool CaravanaBarbara::verificarInimigo(const shared_ptr<Caravana> &outraCaravana) {
    return dynamic_pointer_cast<CaravanaComercio>(outraCaravana) ||
           dynamic_pointer_cast<CaravanaMilitar>(outraCaravana) ||
           dynamic_pointer_cast<CaravanaSecreta>(outraCaravana);
}
