#ifndef TABULEIRO_H
#define TABULEIRO_H
#include "utils.h"
#define MAX_COLS 10
#define MAX_ROWS 10


void printboard(char** tabuleiro, int num_rows, int num_cols);

char expandirboard(char** tabuleiro, int num_rows, int num_cols);

#endif /* TABULEIRO_H */
