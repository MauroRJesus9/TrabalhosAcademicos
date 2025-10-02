#include <windows.h>
#include <tchar.h>
#include <stdio.h>
#include <strsafe.h>
#include <io.h>
#include <conio.h> 
#include <fcntl.h>
#include <process.h>
#include "../utils.h"

BOOL run = true;

typedef struct {
    TCHAR nome[BUFFER_MSG];
    MemoriaPartilhada* shm;
    HANDLE hEventoLetras;
    HANDLE hPipeArbitro;      // Pipe para enviar mensagens ao árbitro
    HANDLE hPipeJogador;      // Pipe para receber mensagens do árbitro
    volatile int terminar;    // flag para sinalizar término das threads
} JogadorData;

void limpaTelaMensagem() {
    _tprintf(TEXT("> "));
    fflush(stdout);
}


DWORD WINAPI esperaMensagens(LPVOID param) {
    JogadorData* d = (JogadorData*)param;
    TCHAR buffer[BUFFER_MSG];
    DWORD bytesRead;

    while (!d->terminar) {
        BOOL success = ReadFile(d->hPipeJogador, buffer, sizeof(buffer) - sizeof(TCHAR), &bytesRead, NULL);
        if (!success || bytesRead == 0) {
            // Pipe fechado ou erro, termina thread
            break;
        }
        if (_tcsncmp(buffer, TEXT("EXCLUIR:"), 8) == 0) {
            _tprintf(TEXT("Foste expulso pelo árbitro. O programa vai terminar.\n"));
            d->terminar = 1;
            CancelIoEx(d->hPipeJogador, NULL); 
            SetEvent(d->hEventoLetras);
            run = false;

            break;
        }else
        if (_tcsncmp(buffer, TEXT("ENCERRAR:"), 9) == 0) {
            d->terminar = 1;
            CancelIoEx(d->hPipeJogador, NULL);
            SetEvent(d->hEventoLetras);
            run = false;

            break;
        }
        // Garantir terminação da string
        buffer[bytesRead / sizeof(TCHAR)] = TEXT('\0');

        _tprintf(TEXT("\n%s\n"), buffer);
        limpaTelaMensagem();
    }
    return 0;
}

DWORD WINAPI esperaLetras(LPVOID param) {
    JogadorData* d = (JogadorData*)param;
    DWORD res;
    while (!d->terminar) {
        res = WaitForSingleObject(d->hEventoLetras, 500);  // Espera 500ms
        if (res == WAIT_OBJECT_0) {
            _tprintf(TEXT("\nLetras disponíveis: "));
            //_tprintf(TEXT("DEBUG: max_letras = %d\n"), d->shm->max_letras);
            for (int i = 0; i < d->shm->max_letras; i++) {
                TCHAR c = d->shm->letras[i] ? d->shm->letras[i] : TEXT('_');
                _tprintf(TEXT("%c "), c);
            }
            _tprintf(TEXT("\n"));
            limpaTelaMensagem();
        }
        else if (res == WAIT_FAILED) {
            _tprintf(TEXT("Erro no WaitForSingleObject: %d\n"), GetLastError());
            break;
        }
    }
    return 0;
}

int _tmain(int argc, TCHAR* argv[]) {
#ifdef UNICODE
    _setmode(_fileno(stdin), _O_WTEXT);
    _setmode(_fileno(stdout), _O_WTEXT);
#endif

    if (argc != 2) {
        _tprintf(TEXT("Uso: %s <nome_jogador>\n"), argv[0]);
        return 1;
    }

    TCHAR* nomeJogador = argv[1];

    // Abrir memória partilhada para letras
    HANDLE hMapFile = OpenFileMapping(FILE_MAP_READ | FILE_MAP_WRITE, FALSE, SHM_NAME);
    if (!hMapFile) {
        _tprintf(TEXT("Erro ao abrir memória partilhada.\n"));
        return 1;
    }
    MemoriaPartilhada* shm = (MemoriaPartilhada*)MapViewOfFile(hMapFile, FILE_MAP_ALL_ACCESS, 0, 0, 0);
    if (!shm) {
        _tprintf(TEXT("Erro ao mapear memória.\n"));
        CloseHandle(hMapFile);
        return 1;
    }


    // Abrir pipe para enviar mensagens ao árbitro (pipe de escrita)
    HANDLE hPipeArbitro = CreateFile(
        PIPE_ARBITRO,
        GENERIC_WRITE,
        0,
        NULL,
        OPEN_EXISTING,
        0,
        NULL);

    if (hPipeArbitro == INVALID_HANDLE_VALUE) {
        _tprintf(TEXT("Erro ao abrir pipe do árbitro. Código: %d\n"), GetLastError());
        UnmapViewOfFile(shm);
        CloseHandle(hMapFile);
        return 1;
    }

    // Enviar nome do jogador ao árbitro
    TCHAR buffer[BUFFER_MSG];
    StringCchPrintf(buffer, BUFFER_MSG, TEXT("NOME:%s"), nomeJogador);
    DWORD bytesWritten;
    BOOL success = WriteFile(hPipeArbitro, buffer, (lstrlen(buffer) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
    if (!success) {
        _tprintf(TEXT("Erro ao enviar nome para árbitro.\n"));
        CloseHandle(hPipeArbitro);
        UnmapViewOfFile(shm);
        CloseHandle(hMapFile);
        return 1;
    }

    // Abrir pipe para receber mensagens do árbitro (pipe de leitura)
    TCHAR pipeNome[BUFFER_MSG];
    StringCchPrintf(pipeNome, BUFFER_MSG, TEXT("%s%s"), PIPE_JOGADOR_BASE, nomeJogador);

    HANDLE hPipeJogador;
    DWORD tentativas = 10;
    do {
        hPipeJogador = CreateFile(
            pipeNome,
            GENERIC_READ,
            0,
            NULL,
            OPEN_EXISTING,
            0,
            NULL);

        if (hPipeJogador != INVALID_HANDLE_VALUE)
            break;

        DWORD err = GetLastError();
        if (err != ERROR_FILE_NOT_FOUND && err != ERROR_PIPE_BUSY) {
            _tprintf(TEXT("Erro inesperado ao abrir pipe do jogador (%d).\n"), err);
            CloseHandle(hPipeArbitro);
            UnmapViewOfFile(shm);
            CloseHandle(hMapFile);
            return 1;
        }

        Sleep(500);
    } while (--tentativas > 0);

    if (hPipeJogador == INVALID_HANDLE_VALUE) {
        _tprintf(TEXT("Timeout: o árbitro não criou o pipe do jogador.\n"));
        CloseHandle(hPipeArbitro);
        UnmapViewOfFile(shm);
        CloseHandle(hMapFile);
        return 1;
    }


    JogadorData d = { 0 };
    _tcscpy_s(d.nome, BUFFER_MSG, nomeJogador);
    d.shm = shm;
    d.hPipeArbitro = hPipeArbitro;
    d.hPipeJogador = hPipeJogador;
    d.terminar = 0;

    d.hEventoLetras = OpenEvent(SYNCHRONIZE, FALSE, EVENTO_LETRAS);
    if (!d.hEventoLetras) {
        _tprintf(TEXT("Erro ao abrir evento das letras. Código: %d\n"), GetLastError());
        UnmapViewOfFile(shm);
        CloseHandle(hMapFile);
        CloseHandle(hPipeArbitro);
        return 1;
    }


    HANDLE thMsg = CreateThread(NULL, 0, esperaMensagens, &d, 0, NULL);
    HANDLE thLetras = CreateThread(NULL, 0, esperaLetras, &d, 0, NULL);

    _tprintf(TEXT("Bem-vindo, %s! Comandos disponíveis: \n:sair\n:pont\n:jogs\nPode também inserir palavras para tentar adivinhar.\n> "), nomeJogador);

    while (run && !d.terminar) {
        if (_kbhit()) {
            if (_fgetts(buffer, BUFFER_MSG, stdin) == NULL) {
                _tprintf(TEXT("\nErro ou fim de ficheiro detectado. A sair...\n"));
                break;
            }

            size_t len = _tcslen(buffer);
            if (len > 0 && buffer[len - 1] == TEXT('\n')) {
                buffer[len - 1] = TEXT('\0');
                len--;
            }

            if (len > 0 && buffer[0] == TEXT(':')) {
                if (_tcscmp(buffer, TEXT(":sair")) == 0) {
                    TCHAR sairMsg[BUFFER_MSG];
                    StringCchPrintf(sairMsg, BUFFER_MSG, TEXT("SAIR:%s"), nomeJogador);
                    WriteFile(d.hPipeArbitro, sairMsg, (lstrlen(sairMsg) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
                    break;
                }
                else if (_tcscmp(buffer, TEXT(":pont")) == 0) {
                    TCHAR pontMsg[BUFFER_MSG];
                    StringCchPrintf(pontMsg, BUFFER_MSG, TEXT("PONT:%s"), nomeJogador);
                    WriteFile(d.hPipeArbitro, pontMsg, (lstrlen(pontMsg) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
                }
                else if (_tcscmp(buffer, TEXT(":jogs")) == 0) {
                    TCHAR jogsMsg[BUFFER_MSG];
                    StringCchPrintf(jogsMsg, BUFFER_MSG, TEXT("JOGS:%s"), nomeJogador);
                    WriteFile(d.hPipeArbitro, jogsMsg, (lstrlen(jogsMsg) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
                }
                else {
                    _tprintf(TEXT("Comando inválido: %s\n"), buffer);
                }
            }
            else if (len > 0) {
                TCHAR msg[BUFFER_MSG];
                StringCchPrintf(msg, BUFFER_MSG, TEXT("MSG:%s:%s"), buffer, nomeJogador);
                WriteFile(d.hPipeArbitro, msg, (lstrlen(msg) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
            }

            limpaTelaMensagem();
        }
        else {
            Sleep(100); // pequena pausa para não consumir CPU excessivamente
        }
    }

    // Sinalizar às threads para terminar
    d.terminar = 1;
    // Cancelar operações bloqueantes nas pipes para as threads terminarem rapidamente
    CancelIoEx(d.hPipeJogador, NULL);
    CancelIoEx(d.hPipeArbitro, NULL);

    WaitForSingleObject(thMsg, INFINITE);
    WaitForSingleObject(thLetras, INFINITE);

    CloseHandle(thMsg);
    CloseHandle(thLetras);

    CloseHandle(d.hPipeJogador);
    CloseHandle(d.hPipeArbitro);
    CloseHandle(d.hEventoLetras);


    UnmapViewOfFile(shm);
    CloseHandle(hMapFile);

    return 0;
}
