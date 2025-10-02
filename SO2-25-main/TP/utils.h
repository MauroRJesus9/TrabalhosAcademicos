#ifndef UTILS_H
#define UTILS_H

#include <windows.h>
#include <tchar.h>
#include <stdbool.h>

#define MAX_LETRAS 10
#define INTERVALO_GERACAO_LETRAS 5  // em segundos
#define MAX_JOGADORES 10
#define BUFFER_MSG 256
#define SHM_NAME TEXT("SHM_LETRAS")
#define MUTEX_LETRAS TEXT("Global\\MutexLetras")
#define EVENTO_LETRAS TEXT("Global\\EventoLetras")
#define PIPE_ARBITRO TEXT("\\\\.\\pipe\\ArbitroPipe")
#define PIPE_JOGADOR_BASE TEXT("\\\\.\\pipe\\Jogador_")

// Estrutura da memória partilhada apenas para letras (Consola) | Descomentar para usar consola com bot e vice-versa
/*typedef struct {
    TCHAR letras[BUFFER_MSG];
    int max_letras;
    int ritmo;
} MemoriaPartilhada;
*/
//Estrutura apenas para Painel - COMENTAR PARA CONSOLA (BOT NAO FUNCIONA COM ISTO) | DESCOMENTAR PARA TESTAR UI Consola

typedef struct {
    TCHAR letras[BUFFER_MSG];
    int max_letras;
    int ritmo;
    TCHAR ultimaPalavra[BUFFER_MSG];

    struct {
        TCHAR nome[BUFFER_MSG];
        float pontos;
    } jogadoresPainel[MAX_JOGADORES];

    int numJogadoresPainel;

} MemoriaPartilhada;


// Adaptação das funções para funcionarem com pipes
void enviaMensagemParaTodos(const TCHAR* msg, const TCHAR* origem);
void limpaMensagem(int indice);  // Esta função pode ser removida se mensagens deixarem de estar em memória partilhada
void carregaDadosJogo(MemoriaPartilhada* shm);
void carregaDicionario(void);
bool existeDicionario(const TCHAR* palavra);

#endif