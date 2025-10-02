//
// Created by mauro on 11/26/2024.
//

#ifndef BUFFER_H
#define BUFFER_H

#include <memory>
#include <string>
using namespace std;

class Buffer {
    int linhas;
    int colunas;
    unique_ptr<char[]> matriz; // Grelha linearizada de caracteres

    int cursorLinha;
    int cursorColuna;

public:
    Buffer(int l, int c);

    void limpar(); // Esvazia o buffer, preenchendo-o com espaços
    void moverCursor(int l, int c); // Move o cursor para uma posição específica
    void escrever(char c); // Escreve um carácter na posição atual do cursor
    static void escrever(const string& str); // Escreve uma string na posição atual do cursor
    void mostrar(); // Mostra o conteúdo do buffer no terminal

    int getLinhas() const { return linhas; }
    int getColunas() const { return colunas; }
    char getChar(int l, int c) const;
    void operator<<(const string& str);
};

#endif // BUFFER_H
