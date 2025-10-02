#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <math.h>
#include "utils.h"
#include "tabuleiro.h"
#include "jogo.h"

#define MAX_COLS 10
#define MAX_ROWS 10
void jogar(pjogadas p, char** tabuleiro, int num_rows, int num_cols,int nronda,int modo) {
    int row, col;
    int validar = 0;
    do {
        if (nronda%2 == 0 && modo == 2) {
            row = intUniformRnd(0, num_rows);
            col = intUniformRnd(0, num_cols);
        }
        else {
            printf("Escolha uma linha e uma coluna para jogar: ");
            scanf_s("%d %d", &row, &col);
        }
            for (int i = 0; i < num_rows; i++) {
                if (row == i) {
                    for (int j = 0; j < num_cols; j++) {
                        if (col == j) {
                            validar = 1;
                            if (tabuleiro[i][j] == '\0') {
                                tabuleiro[i][j] = 'G';
                            }
                            else {
                                if (tabuleiro[i][j] == 'G') {
                                    tabuleiro[i][j] = 'Y';
                                }
                                else {
                                    if (tabuleiro[i][j] == 'Y') {
                                        tabuleiro[i][j] = 'R';
                                    }
                                    else {
                                        validar = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        if (validar == 0 && nronda%2!=0) printf("Jogada invalida\n");
    } while (validar == 0);
}
void jogar_pedra(pjogadas p, char** tabuleiro, int num_rows, int num_cols, int nronda) {
    int row, col;
    int validar = 0;
    do {
        printf("Escolha uma linha e uma coluna para jogar: ");
        scanf_s("%d %d", &row, &col);
        for (int i = 0; i < num_rows; i++) {
            if (row == i) {
                for (int j = 0; j < num_cols; j++) {
                    if (col == j) {
                        if (tabuleiro[i][j] == '\0') {
                            tabuleiro[i][j] = 'P';
                            validar = 1;
                        }
                    }
                }
            }
        }
        if (validar == 0) printf("Jogada invalida\n");
    } while (validar == 0);
}
pjogadas insere_final(pjogadas p, char** tabuleiro, int num_rows, int num_cols, int nronda) {
    pjogadas novo, aux;
    novo = malloc(sizeof(jogadas)); 
    if (novo == NULL) { 
        printf("Erro na alocacao de memoria\n"); 
        return p; 
    }
    for (int i = 0; i < num_rows; i++) {
        for (int j = 0; j < num_cols; j++) {
            novo->board[i][j]=tabuleiro[i][j];
        }
    }
    if (nronda % 2 != 0) {
        novo->c_jogador = 'A';
    }
    else {
        novo->c_jogador = 'B';
    }
    
    novo->n_jogada = nronda;
    novo->prox = NULL;
    if (p == NULL)p = novo; 
    else { 
        aux = p; 
        while (aux->prox != NULL)
            aux = aux->prox; 
            aux->prox = novo; 
    }
    return p;
}
void escreve_ficheiro(pjogadas p, int num_rows, int num_cols) {
    FILE* f;
    char aux,fpath[100];
    printf("\nQual o nome do ficheiro em que quer guardar os dados:");
    scanf("%s", &fpath);
    f = fopen(fpath, "w");
    if (f == NULL) {
        printf("Nao foi possivel aceder ao ficheiro");
    }
    else {
        while (p != NULL)
        {
            fprintf(f, "Jogador: %c \n\n", p->c_jogador);
            for (int i = 0; i < num_rows; i++) {
                for (int j = 0; j < num_cols; j++) {
                    aux = p->board[i][j];
                    fprintf(f, " |%c| ", aux);
                }
                fprintf(f, "\n");
            }
            fprintf(f, "\n\n");
            p = p->prox;
        }
    }
    fclose(f);
}
void percorrejogadas(pjogadas p, int num_rows, int num_cols, int nronda)
{
    int niter=0;
    int z = 1;
    do {
        printf("\Quantas iteracoes pretende voltar?");
        scanf_s("%d", &niter);
    } while (niter>=nronda);
    while (p != NULL)
    {
        if (z=nronda-niter) {
            for (int i = 0; i < num_rows; i++) {
                for (int j = 0; j < num_cols; j++) {
                    printf(" |%c| ", p->board[i][j]);
                }
                printf("\n");
            }
            printf("\n");
        }
        else {
            z++;
        }
        p = p->prox;
        
    }
}
void guardarbinario(char** tabuleiro, int num_rows, int num_cols, int nronda) {
    FILE* f;
    int i;
    f = fopen("jogo.bin", "wb");
    if (f == NULL)
    {
        printf("Erro no acesso ao ficheiro\n");
        return;
    }
    fwrite(&nronda, sizeof(int), 1, f);
    fwrite(&num_rows, sizeof(int), 1, f);
    fwrite(&num_cols, sizeof(int), 1, f);
    fwrite(tabuleiro, sizeof(char), (num_rows * num_cols), f);
    fclose(f);
}
void carregarbinario(char** tabuleiro, int* num_rows, int* num_cols, int* nronda) {
    FILE* f;
    int i,auxrows,auxcols,auxload;
    f = fopen("jogo.bin", "rb");
    if (f == NULL)
    {
        printf("Erro no acesso ao ficheiro\n");
        return;
    }
    for (int i = 0; i < num_rows; i++) {
        for (int j = 0; j < num_cols; j++) {
            tabuleiro[i][j] = '\0';
            free(tabuleiro[i][j]);
        }
    }
    
    fread(&nronda, sizeof(int), 1, f);
    fread(&num_rows, sizeof(int), 1, f);
    fread(&num_cols, sizeof(int), 1, f);
    auxload = num_rows;
    auxrows = num_rows;
    auxcols = num_cols;
    /*tabuleiro = realloc(tabuleiro, (auxrows + 1) * sizeof(char*)); // Allocate row pointers
    for (int i = 0; i < auxload-auxrows; i++)
        tabuleiro[i] = realloc(tabuleiro[i], auxcols + 1 * sizeof(char));
    char tabuleiroaux[100];
    fread(tabuleiroaux, sizeof(char), 1, f);
    for (int i = 0; i < auxrows; i++)
    {
        for (int j = 0; j < auxcols; j++)
        {
            tabuleiro[i][j] = tabuleiroaux[i * auxrows + j];
            printf("|%c|", tabuleiro[i][j]);
        }
        printf("\n");
    }
    //fread(&tabuleiro, sizeof(char), 1, f);*/
    fclose(f);
}
int escolher_jogada() {
    int n;
    printf("\nQue jogada pretende fazer?\n");
    printf("1 - Jogada normal\n");
    printf("2 - Jogar pedra\n");
    printf("3 - Expandir tabuleiro\n");
    printf("4 - Voltar iteracoes\n");
    printf("5 - Sair\n");
    do {
        printf("\nOpcao:");
        scanf_s("%d", &n);
    } while (n < 1 || n > 5);
    return n;
}
void liberta_lista(pjogadas p)
{
    pjogadas aux;
    while (p != NULL) {
        aux = p; p = p->prox;
        free(aux);
    }
}
int fimjogo(char** tabuleiro, int num_rows, int num_cols, int nronda) {
    int checkerl = 1,checkerc=1,checker=0,empate=1, checkerd = 0,tam=0;
    char cor;
    for (int i = 0; i < num_rows - 1; i++) {
        for (int j = 0; j < num_cols - 1; j++) {
            if (tabuleiro[i][j] == tabuleiro[i][j + 1] && tabuleiro[i][j] != '\0') {
                checkerl++;
            }
            if (tabuleiro[i][j] == tabuleiro[i + 1][j] && tabuleiro[i][j] != '\0') { 
                checkerc++;
            }
            if (tabuleiro[i][j] == '\0' || tabuleiro[i][j] == 'G' || tabuleiro[i][j] == 'Y') {
                empate = 0;
            }
        }
        if (tabuleiro[0][0] != '\0')
        {
            cor = tabuleiro[0][0];
            checkerd = 1;
            if (num_rows> num_cols) {
                tam = num_rows;
            }
            else {
                tam = num_cols;
            }
            for (int i = 1; i < tam; i++)
            {
                if (tabuleiro[i][i] == cor)
                {
                    checkerd++;
                }
                else
                    break;

            }

            if (checkerd == tam)
            {
                checker = 1;
            }
            else
                checker = 0;
            }

        //Diagonal Secundaria /
        if (tabuleiro[0][tam - 1] != '\0')
        {
            cor = tabuleiro[0][0];
            checkerd = 1;

            for (int i = 1; i < tam; i++)
            {
                if (tabuleiro[i][(tam - 1) - i] == cor)
                {
                    checkerd++;
                }
                else
                    break;

            }

            if (checkerd == tam)
            {
                checker = 1;
            }
            else
                checker = 0;
        }
    }
    if ((checkerc == num_rows || checkerl == num_cols) || checker == 1) {
        checker = 1;
        if (nronda%2 != 0) {
            printf("Jogador A venceu");
        }
        else {
            printf("Jogador B venceu");
        }
    }
    else {
        if (empate == 1 && checker != 1) {
            checker = 1;
            printf("Empate!");
        }
    }
    return checker;
}