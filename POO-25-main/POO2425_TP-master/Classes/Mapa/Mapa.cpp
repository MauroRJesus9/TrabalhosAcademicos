//
// Created by mauro on 11/26/2024.
//

#include <iostream>
#include "Mapa.h"

Mapa::Mapa(int l, int c) : linhas(l), colunas(c), grelha(l, vector<shared_ptr<Zona>>(c, nullptr)) {}

void Mapa::definirZona(int linha, int coluna, shared_ptr<Zona> zona) {
    if (linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas)
        grelha[linha][coluna] = std::move(zona);
}

shared_ptr<Zona> Mapa::obterZona(int linha, int coluna) const {
    if (linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas)
        return grelha[linha][coluna];
    return nullptr;
}

std::shared_ptr<Zona> Mapa::obterZonaPorNome(const char& nome) const {
    for (int i = 0; i < getLinhas(); i++) {
        for (int j = 0; j < getColunas(); j++) {
            auto zona = obterZona(i, j);
            if (zona && zona->getTipo() == 'C' && zona->getTipo() == nome) {
                return zona;
            }
        }
    }
    return nullptr;
}

bool Mapa::zonaValida(int linha, int coluna) const {
    return linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas;
}

void Mapa::mostrar(Buffer& buffer) {
    buffer.limpar();
    for (int i = 0; i < linhas; ++i) {
        for (int j = 0; j < colunas; ++j) {
            if (grelha[i][j])
                buffer.moverCursor(i, j), buffer.escrever(grelha[i][j]->getTipo());
            else
                buffer.moverCursor(i, j), buffer.escrever('.');
        }
    }
    buffer.mostrar();
}

int Mapa::getLinhas() const {
    return linhas;
}

void Mapa::setLinhas(int linhas) {
    Mapa::linhas = linhas;
}

int Mapa::getColunas() const {
    return colunas;
}

void Mapa::setColunas(int colunas) {
    Mapa::colunas = colunas;
}

void Mapa::removerElemento(int linha, int coluna) {
    if (zonaValida(linha, coluna)) {
        grelha[linha][coluna] = make_shared<Deserto>();
        std::cout << "Elemento removido da posição (" << linha << ", " << coluna << ") e substituído por Deserto." << std::endl;
    }
}

void Mapa::colocarItem(int linha, int coluna, std::shared_ptr<Itens> item) {
    if (linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas) {
        grelha[linha][coluna] = item;
    }
}

bool Mapa::estaAdjacente(int linha1, int coluna1, int linha2, int coluna2) {
    return (abs(linha1 - linha2) + abs(coluna1 - coluna2)) == 1;
}

