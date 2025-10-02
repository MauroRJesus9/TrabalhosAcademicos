#ifndef JOGO_H
#define JOGO_H
#include "utils.h"
#include "tabuleiro.h"


typedef struct jogada jogadas, *pjogadas;
struct jogada {
    char board[10][10];
    int n_jogada;
    char c_jogador;
    pjogadas prox;
};

void jogar(pjogadas p, char** tabuleiro, int num_rows, int num_cols, int nronda, int modo);

void jogar_pedra(pjogadas p, char** tabuleiro, int num_rows, int num_cols, int nronda);

pjogadas insere_final(pjogadas p, char** tabuleiro, int num_rows, int num_cols, int nronda);

void escreve_ficheiro(pjogadas p, int num_rows, int num_cols);

void percorrejogadas(pjogadas p, int num_rows, int num_cols, int nronda);

void guardarbinario(char** tabuleiro, int num_rows, int num_cols, int nronda);

void carregarbinario(char** tabuleiro, int* num_rows, int* num_cols, int* nronda);

int escolher_jogada();

void liberta_lista(pjogadas p);

int fimjogo(char** tabuleiro, int num_rows, int num_cols, int nronda);
#endif /* JOGO_H */
