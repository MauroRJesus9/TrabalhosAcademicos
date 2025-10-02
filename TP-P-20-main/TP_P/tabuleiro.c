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


void printboard(char** tabuleiro, int num_rows, int num_cols) {
    for (int i=0; i < num_rows;i++) {
        for (int j=0; j < num_cols; j++) {
            printf(" |%c| ", tabuleiro[i][j]);
        }
        printf("\n");
    }
    printf("\n\n");
}
char expandirboard(char** tabuleiro, int num_rows, int num_cols) {
    char escolha, aux[10][10];
    int new=0;
    printf("Pretende adicionar linha ou coluna?");
    scanf(" %c", &escolha);
    if (escolha=='c' || escolha == 'C') {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                aux[i][j] = '\0';
            }
        }
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_cols; j++) {
                aux[i][j] = tabuleiro[i][j];
            }
        }
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_cols; j++) {
                tabuleiro[i][j] = '\0';
                free(tabuleiro[i][j]);
            }
        }
        for (int i = 0; i < num_rows; i++)
            tabuleiro[i] = realloc(tabuleiro[i],num_cols+1 * sizeof(char));
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_cols+1; j++) {
                tabuleiro[i][j] = aux[i][j];
            }
        }
    }
    else {
        if (escolha == 'l' || escolha == 'L') {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    aux[i][j] = '\0';
                }
            }
            for (int i = 0; i < num_rows; i++) {
                for (int j = 0; j < num_cols; j++) {
                    aux[i][j] = tabuleiro[i][j];
                }
            }
            for (int i = 0; i < num_rows; i++) {
                for (int j = 0; j < num_cols; j++) {
                    tabuleiro[i][j] = '\0';
                    free(tabuleiro[i][j]);
                }
            }
            tabuleiro = realloc(tabuleiro,(num_rows+1) * sizeof(char*)); // Allocate row pointers
            for (int i = 0; i < 1; i++)
                tabuleiro[num_rows + i] = malloc(num_cols * sizeof(char));
            for (int i = 0; i < num_rows+1; i++) {
                for (int j = 0; j < num_cols; j++) {
                    tabuleiro[i][j] = aux[i][j];
                }
            }
        }
    }
    return escolha;
}
