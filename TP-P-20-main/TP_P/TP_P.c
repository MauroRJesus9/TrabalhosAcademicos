#include<stdio.h>
#include<string.h>
#include<math.h>
#include<time.h>
#include "jogo.h"
#include "tabuleiro.h"
#include "utils.h"

int menu() {
    int n;
    puts("\n");
    puts("1 - Jogar 2 jogadores");
    puts("2 - Jogar 1 jogador");
    puts("3 - Sair");
    do {
        printf("\nOpcao:");
        scanf_s("%d", &n);
    } while (n < 1 || n > 3);
    return n;
}

int main() {
	srand(time(NULL));
    int n;
    char a;
    pjogadas jogada = NULL;
    
    do {
        char exp;
        int num_rows = intUniformRnd(3, 5);
        int num_cols = intUniformRnd(3, 5);
        int p = 0, modo = 0, fim = 0, pedra_A = 1, pedra_B = 1, exp_A = 2, exp_B = 2,load=0;
        char** tabuleiro = malloc(num_rows * sizeof(char*)); // Allocate row pointers
        for (int i = 0; i < num_rows; i++)
            tabuleiro[i] = malloc(num_cols * sizeof(char));
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_cols; j++) {
                tabuleiro[i][j] = '\0';
            }
        }
        if (load != 1) {
            FILE* file;
            if (file = fopen("jogo.bin", "r")) {
                fclose(file);
                do {
                    printf("\nDeseja carregar o jogo que foi deixado a meio anteriormente?(s/n)");
                    scanf_s("%c", &a);
                    if (a == 's' || a == 'S') {
                        carregarbinario(tabuleiro, num_rows, num_cols, p);
                        load = 1;
                    }
                    else {
                        if (a == 'n' || a == 'N') {
                            load = 1;
                        }
                    }
                } while (a != 's' && a != 'S' && a != 'n' && a != 'N');

            }
            else {
                load = 1;
            }
        }
        n = menu();
        
        switch (n) {
            case 1: 
                do {
                    p++;
                    modo = escolher_jogada();
                    switch (modo) {
                        case 1: jogar(jogada, tabuleiro, num_rows, num_cols,p,n);
                                printboard(tabuleiro, num_rows, num_cols);break;
                        case 2: if (p%2!=0 && pedra_A==1) {
                                    pedra_A = 0;
                                    jogar_pedra(jogada, tabuleiro, num_rows, num_cols, p);
                                    printboard(tabuleiro, num_rows, num_cols);
                                }
                                else {
                                    if (p % 2 == 0 && pedra_B == 1) {
                                        pedra_B = 0;
                                        jogar_pedra(jogada, tabuleiro, num_rows, num_cols, p);
                                        printboard(tabuleiro, num_rows, num_cols);
                                    }
                                    else {
                                        printf("Nao tem mais pedras para jogar\n");
                                        jogar(jogada, tabuleiro, num_rows, num_cols, p,n);
                                        printboard(tabuleiro, num_rows, num_cols);
                                    }
                                }break;
                        case 3: 
                            if (p % 2 != 0 && exp_A > 0) {
                                exp_A--;
                                exp=expandirboard(tabuleiro, num_rows, num_cols);
                            }
                            else {
                                if (p % 2 == 0 && exp_B > 0) {
                                    exp_B--;
                                    exp=expandirboard(tabuleiro, num_rows, num_cols);
                                }
                                else {
                                    printf("Nao tem mais expansoes para jogar\n");
                                    jogar(jogada, tabuleiro, num_rows, num_cols, p,n);
                                }
                            }
                            if (exp == 'l' || exp == 'L') {
                                num_rows++;
                            }
                            else {
                                if (exp == 'c' || exp == 'C') {
                                    num_cols++;
                                }
                            }
                            exp = '\0';
                                printboard(tabuleiro, num_rows, num_cols); break;
                        case 4: if (p > 1) { percorrejogadas(jogada, num_rows, num_cols, p); }
                              else { printf("Nao pode voltar iteracoes na 1a ronda"); }; break;
                        case 5: guardarbinario(tabuleiro, num_rows, num_cols, p); fim = 1; break;
                    }
                    if (modo!=4) {
                        jogada = insere_final(jogada, tabuleiro, num_rows, num_cols, p);
                    }
                    if (modo != 5) {
                        fim = fimjogo(tabuleiro, num_rows, num_cols, p);
                    }
                } while (fim == 0);
                if (modo != 5) {
                    escreve_ficheiro(jogada, num_rows, num_cols);
                }
                
                liberta_lista(jogada);
                for (int i = 0; i < num_rows; i++) {
                    for (int j = 0; j < num_cols; j++) {
                        tabuleiro[i][j] = '\0';
                        free(tabuleiro[i][j]);
                    }
                }
                break;
            case 2: do {
                p++;
                if (p % 2 == 0) {
                    jogar(jogada, tabuleiro, num_rows, num_cols, p,n);
                    printboard(tabuleiro, num_rows, num_cols);
                }
                else {
                    modo = escolher_jogada();
                    switch (modo) {
                    case 1: jogar(jogada, tabuleiro, num_rows, num_cols, p,n);
                        printboard(tabuleiro, num_rows, num_cols); break;
                    case 2:
                        if (p % 2 != 0 && pedra_A == 1) {
                            pedra_A = 0;
                            jogar_pedra(jogada, tabuleiro, num_rows, num_cols, p);
                            printboard(tabuleiro, num_rows, num_cols);
                        }
                        else {
                            printf("Nao tem mais pedras para jogar\n");
                            jogar(jogada, tabuleiro, num_rows, num_cols, p,n);
                            printboard(tabuleiro, num_rows, num_cols);
                        }break;
                    case 3:
                        if (p % 2 != 0 && exp_A > 0) {
                            exp_A--;
                            exp = expandirboard(tabuleiro, num_rows, num_cols);
                        }
                        else {
                            printf("Nao tem mais expansoes para jogar\n");
                            jogar(jogada, tabuleiro, num_rows, num_cols, p,n);
                        }
                        if (exp == 'l' || exp == 'L') {
                            num_rows++;
                        }
                        else {
                            if (exp == 'c' || exp == 'C') {
                                num_cols++;
                            }
                        }
                        exp = '\0';
                        printboard(tabuleiro, num_rows, num_cols); break;
                    case 4: if (p > 1) { percorrejogadas(jogada, num_rows, num_cols, p); }
                          else { printf("Nao pode voltar iteracoes na 1a ronda"); }; break;
                    case 5: guardarbinario(tabuleiro, num_rows, num_cols, p); fim = 1; break;
                    }
                    if (modo != 4) {
                        jogada = insere_final(jogada, tabuleiro, num_rows, num_cols, p);
                    }
                    if (modo !=5) {
                        fim = fimjogo(tabuleiro, num_rows, num_cols, p);
                    }
                }
            } while (fim == 0);
            escreve_ficheiro(jogada, num_rows, num_cols);
            liberta_lista(jogada);
            for (int i = 0; i < num_rows; i++) {
                for (int j = 0; j < num_cols; j++) {
                    tabuleiro[i][j] = '\0';
                    free(tabuleiro[i][j]);
                }
            }break;
            case 3: break;
        }
    } while (n != 3);
    
}