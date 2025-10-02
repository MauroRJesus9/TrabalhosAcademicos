#include "Buffer.h"
#include <iostream>

Buffer::Buffer(int l, int c) : linhas(l), colunas(c), cursorLinha(0), cursorColuna(0) {
    matriz = make_unique<char[]>(linhas * colunas);
    limpar();
}

void Buffer::limpar() {
    for (int i = 0; i < linhas * colunas; ++i)
        matriz[i] = ' ';
}

void Buffer::moverCursor(int l, int c) {
    if (l >= 0 && l < linhas && c >= 0 && c < colunas) {
        cursorLinha = l;
        cursorColuna = c;
    }
}

void Buffer::escrever(char c) {
    if (cursorLinha >= 0 && cursorLinha < linhas && cursorColuna >= 0 && cursorColuna < colunas) {
        matriz[cursorLinha * colunas + cursorColuna] = c;
        if (cursorColuna + 1 < colunas)
            ++cursorColuna;
    }
}

void Buffer::escrever(const string& str) {
    cout << str << endl;
}

void Buffer::mostrar() {
    cout << endl << "------ Mapa Atualizado ------" << endl;
    for (int i = 0; i < linhas; ++i) {
        for (int j = 0; j < colunas; ++j) {
            cout << matriz[i * colunas + j];
        }
        cout << endl;
    }
    cout << "----------------------------" << endl;
}

char Buffer::getChar(int l, int c) const {
    if (l >= 0 && l < linhas && c >= 0 && c < colunas) {
        return matriz[l * colunas + c];
    }
    return ' ';
}

void Buffer::operator<<(const string& str) {
    for (char c : str) {
        escrever(c);
    }
}