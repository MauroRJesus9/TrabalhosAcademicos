#include <windows.h>
#include <tchar.h>
#include <stdio.h>
#include <strsafe.h>
#include <fcntl.h>
#include <io.h>
#include <process.h>
#include <time.h>
#include "../utils.h"

#define DICIONARIO_PATH TEXT("dicionario.txt")
#define MAX_PALAVRAS 1000

TCHAR dicionario[MAX_PALAVRAS][BUFFER_MSG];
int totalPalavras = 0;

void carregarDicionario() {
    FILE* f = _tfopen(DICIONARIO_PATH, TEXT("r"));
    if (!f) {
        _tprintf(TEXT("[BOT]Erro ao abrir dicionario.txt\n"));
        ExitProcess(1);
    }

    while (_fgetts(dicionario[totalPalavras], BUFFER_MSG, f) && totalPalavras < MAX_PALAVRAS) {
        size_t len = _tcslen(dicionario[totalPalavras]);
        if (dicionario[totalPalavras][len - 1] == TEXT('\n'))
            dicionario[totalPalavras][len - 1] = TEXT('\0');
        totalPalavras++;
    }
    fclose(f);
}

BOOL letrasDisponiveis(const TCHAR* palavra, MemoriaPartilhada* shm) {
    int letrasMemoria[26] = { 0 };
    for (int i = 0; i < shm->max_letras; i++) {
        TCHAR letra = shm->letras[i];
        if (letra >= TEXT('A') && letra <= TEXT('Z')) {
            letrasMemoria[letra - TEXT('A')]++;
        }
        else if (letra >= TEXT('a') && letra <= TEXT('z')) {
            letrasMemoria[letra - TEXT('a')]++;
        }
    }
    int letrasPalavra[26] = { 0 };
    for (int i = 0; palavra[i] != TEXT('\0'); i++) {
        TCHAR c = palavra[i];
        if (c >= TEXT('A') && c <= TEXT('Z'))
            letrasPalavra[c - TEXT('A')]++;
        else if (c >= TEXT('a') && c <= TEXT('z'))
            letrasPalavra[c - TEXT('a')]++;
        else
            return FALSE;
    }
    for (int i = 0; i < 26; i++) {
        if (letrasPalavra[i] > letrasMemoria[i]) return FALSE;
    }
    return TRUE;
}

int _tmain(int argc, TCHAR* argv[]) {
#ifdef UNICODE
    _setmode(_fileno(stdin), _O_WTEXT);
    _setmode(_fileno(stdout), _O_WTEXT);
#endif

    if (argc != 3) {
        _tprintf(TEXT("[BOT]Uso: %s <nome_bot> <tempo_reacao>\n"), argv[0]);
        return 1;
    }

    TCHAR* nomeBot = argv[1];
    int tempoReacao = _ttoi(argv[2]);

    srand((unsigned)time(NULL));

    HANDLE hMapFile = OpenFileMapping(FILE_MAP_READ, FALSE, SHM_NAME);
    if (!hMapFile) {
        _tprintf(TEXT("[BOT]Erro ao abrir memoria partilhada.\n"));
        return 1;
    }
    MemoriaPartilhada* shm = (MemoriaPartilhada*)MapViewOfFile(hMapFile, FILE_MAP_READ, 0, 0, 0);
    if (!shm) {
        _tprintf(TEXT("[BOT]Erro ao mapear memoria partilhada.\n"));
        CloseHandle(hMapFile);
        return 1;
    }

    HANDLE hPipeArbitro = CreateFile(PIPE_ARBITRO, GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
    if (hPipeArbitro == INVALID_HANDLE_VALUE) {
        _tprintf(TEXT("[BOT]Erro ao abrir pipe do arbitro.\n"));
        return 1;
    }

    TCHAR msg[BUFFER_MSG];
    DWORD bytesWritten;
    StringCchPrintf(msg, BUFFER_MSG, TEXT("NOME:%s"), nomeBot);
    WriteFile(hPipeArbitro, msg, (lstrlen(msg) + 1) * sizeof(TCHAR), &bytesWritten, NULL);

    TCHAR pipeNome[BUFFER_MSG];
    StringCchPrintf(pipeNome, BUFFER_MSG, TEXT("%s%s"), PIPE_JOGADOR_BASE, nomeBot);
    HANDLE hPipeJogador = INVALID_HANDLE_VALUE;
    for (int i = 0; i < 10; i++) {
        hPipeJogador = CreateFile(pipeNome, GENERIC_READ, 0, NULL, OPEN_EXISTING, 0, NULL);
        if (hPipeJogador != INVALID_HANDLE_VALUE) break;
        Sleep(500);
    }
    if (hPipeJogador == INVALID_HANDLE_VALUE) {
        _tprintf(TEXT("[BOT]Erro: timeout ao ligar pipe do bot.\n"));
        return 1;
    }

    HANDLE hEvento = OpenEvent(SYNCHRONIZE, FALSE, EVENTO_LETRAS);
    if (!hEvento) {
        _tprintf(TEXT("[BOT]Erro ao abrir evento das letras.\n"));
        return 1;
    }

    carregarDicionario();
    _tprintf(TEXT("[BOT] Bot Nome: %s e tempo de reacao: %d segundos iniciado.\n"), nomeBot, tempoReacao);

    TCHAR buffer[BUFFER_MSG];
    DWORD bytesRead;
    BOOL terminar = FALSE;

    while (!terminar) {
        if (WaitForSingleObject(hEvento, 5000) == WAIT_OBJECT_0) {
            Sleep(tempoReacao * 1000);
            for (int i = 0; i < totalPalavras; i++) {
                if (letrasDisponiveis(dicionario[i], shm)) {
                    StringCchPrintf(msg, BUFFER_MSG, TEXT("MSG:%s:%s"), dicionario[i], nomeBot);
                    WriteFile(hPipeArbitro, msg, (lstrlen(msg) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
                    _tprintf(TEXT("[BOT] %s tentou palavra: %s\n"), nomeBot, dicionario[i]);
                    break;
                }
            }
        }
        if (ReadFile(hPipeJogador, buffer, sizeof(buffer), &bytesRead, NULL)) {
            buffer[bytesRead / sizeof(TCHAR)] = TEXT('\0');
            if (_tcsncmp(buffer, TEXT("EXCLUIR:"), 8) == 0 || _tcsncmp(buffer, TEXT("ENCERRAR:"), 9) == 0) {
                _tprintf(TEXT("[BOT]Bot %s recebeu sinal para terminar.\n"), nomeBot);
                terminar = TRUE;
            }
        }
    }

    CloseHandle(hPipeJogador);
    CloseHandle(hPipeArbitro);
    CloseHandle(hEvento);
    UnmapViewOfFile(shm);
    CloseHandle(hMapFile);
    return 0;
}
