//
// Created by mauro on 11/26/2024.
//

#include "Caravana.h"
#include <iostream>

Caravana::Caravana(int id, int linha, int coluna)
        : id(id), tipo(tipo), linha(linha), coluna(coluna), tripulantes(0),
          capacidadeCarga(0), cargaAtual(0), capacidadeAgua(0), aguaAtual(0), deslocacaoPturno(0), gastAgua(0), isAutonomo(false) {}

bool Caravana::mover(const string& direcao, const int& dist, int maxLinhas, int maxColunas) {
    if (dist > deslocacaoPturno) {
        std::cout << "Caravana não pode andar mais do que " << deslocacaoPturno << " unidades por turno." << std::endl;
        return false;
    }

    if (direcao == "N") { // Norte: para cima, diminui linha
        linha = (linha - dist + maxLinhas) % maxLinhas;
    } else if (direcao == "NE") { // Nordeste: diminui linha, aumenta coluna
        linha = (linha - dist + maxLinhas) % maxLinhas;
        coluna = (coluna + dist) % maxColunas;
    } else if (direcao == "E") { // Leste: para direita, aumenta coluna
        coluna = (coluna + dist) % maxColunas;
    } else if (direcao == "SE") { // Sudeste: aumenta linha, aumenta coluna
        linha = (linha + dist) % maxLinhas;
        coluna = (coluna + dist) % maxColunas;
    } else if (direcao == "S") { // Sul: para baixo, aumenta linha
        linha = (linha + dist) % maxLinhas;
    } else if (direcao == "SW") { // Sudoeste: aumenta linha, diminui coluna
        linha = (linha + dist) % maxLinhas;
        coluna = (coluna - dist + maxColunas) % maxColunas;
    } else if (direcao == "W") { // Oeste: para esquerda, diminui coluna
        coluna = (coluna - dist + maxColunas) % maxColunas;
    } else if (direcao == "NW") { // Noroeste: diminui linha, diminui coluna
        linha = (linha - dist + maxLinhas) % maxLinhas;
        coluna = (coluna - dist + maxColunas) % maxColunas;
    } else {
        return false;
    }

    return true;
}

void Caravana::comportamentoGeralAutonomo() {
// Movimento aleatório genérico
    static const int deslocX[] = {0, 1, 1, 1, 0, -1, -1, -1};
    static const int deslocY[] = {1, 1, 0, -1, -1, -1, 0, 1};

    srand(static_cast<unsigned int>(time(nullptr)));
    int direcao = rand() % 8;
    linha = (linha + deslocX[direcao] + 100) % 100; // Assume dimensões arbitrárias
    coluna = (coluna + deslocY[direcao] + 100) % 100;
}

bool Caravana::afetarPorTempestade() {
    perderTripulantes(1);  // Perde um tripulante por tempestade
    if (estaSemTripulantes()) {
        std::cout << "Caravana " << id << " destruída pela tempestade!" << std::endl;
        return true; // Indica que a comandoCaravana foi destruída
    }
    return false;
}

void Caravana::perderTripulantes(int quantidade) {
    tripulantes -= quantidade;
    if (tripulantes < 0) tripulantes = 0;
}

void Caravana::adicionarTripulantes(int quantidade) {
    tripulantes += quantidade;
    if (tripulantes > capacidadeCarga) tripulantes = capacidadeCarga;
}

void Caravana::aumentarCargaTemporaria(int quantidade) {
    cargaAtual += quantidade;
    if (cargaAtual > capacidadeCarga) cargaAtual = capacidadeCarga;
}

int Caravana::getAguaAtual() const {
    return aguaAtual;
}

int Caravana::getDeslocacaoPturno() const {
    return deslocacaoPturno;
}

int Caravana::getGastAgua() const {
    return gastAgua;
}

int Caravana::getCapacidadeAgua() const {
    return capacidadeAgua;
}

int Caravana::getCapacidadeCarga() const {
    return capacidadeCarga;
}

void Caravana::adicionarTripulantesComando(int quantidade) {
    if (quantidade <= 0) {
        std::cout << "Erro: Quantidade de tripulantes inválida.\n";
        return;
    }

    tripulantes += quantidade;
    if (tripulantes > capacidadeCarga) {
        tripulantes = capacidadeCarga;
        std::cout << "Capacidade de tripulantes atingida. Excesso ignorado.\n";
    }

    std::cout << "Adicionados " << quantidade << " tripulantes à caravana ID: " << id << ".\n";
}

int Caravana::getTurnosSemTripulantes() const {
    return turnosSemTripulantes;
}

void Caravana::setTurnosSemTripulantes(int turnosSemTripulantes) {
    Caravana::turnosSemTripulantes = turnosSemTripulantes;
}
