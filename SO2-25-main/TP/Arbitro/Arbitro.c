#include <windows.h>
#include <tchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <strsafe.h>
#include <io.h>
#include <fcntl.h>
#include <stdbool.h>
#include <time.h>
#include "../utils.h"

typedef struct {
    TCHAR nome[BUFFER_MSG];
    HANDLE hPipeJogador;  // Pipe para enviar mensagens a este jogador
    float pontos;
} Jogador;

static Jogador jogadores[MAX_JOGADORES];
static int numJogadores = 0;


BOOL letrasIniciadas = FALSE;
CRITICAL_SECTION csLetras;

static HANDLE hMapFile = NULL;
HANDLE hThreadLetras;
HANDLE hMutexLetras;
HANDLE hEventoLetras;
static MemoriaPartilhada* shm = NULL;

static CRITICAL_SECTION csJogadores;  // Critical section para sincronizar acesso a jogadores

void fecharJogador(int indice) {
    if (indice < 0 || indice >= numJogadores) return;

    if (jogadores[indice].hPipeJogador != INVALID_HANDLE_VALUE) {
        FlushFileBuffers(jogadores[indice].hPipeJogador);
        DisconnectNamedPipe(jogadores[indice].hPipeJogador);
        CloseHandle(jogadores[indice].hPipeJogador);
    }

    // Remover jogador do array - operação sincronizada
    EnterCriticalSection(&csJogadores);
    for (int i = indice; i < numJogadores - 1; i++) {
        jogadores[i] = jogadores[i + 1];
    }
    numJogadores--;
    LeaveCriticalSection(&csJogadores);
}

void enviaMensagemParaTodos(const TCHAR* msg, const TCHAR* origem) {
    TCHAR buffer[BUFFER_MSG + 100];
    if (origem && origem[0] != TEXT('\0'))
        StringCchPrintf(buffer, BUFFER_MSG + 100, TEXT("[%s] %s"), origem, msg);
    else
        StringCchCopy(buffer, BUFFER_MSG + 100, msg);

    EnterCriticalSection(&csJogadores);
    for (int i = 0; i < numJogadores; i++) {
        if (jogadores[i].hPipeJogador != INVALID_HANDLE_VALUE) {
            DWORD bytesWritten = 0;
            BOOL success = WriteFile(jogadores[i].hPipeJogador, buffer, (lstrlen(buffer) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
            if (!success) {
                _tprintf(TEXT("Erro a enviar mensagem ao jogador %s.\n"), jogadores[i].nome);
            }
        }
    }
    LeaveCriticalSection(&csJogadores);
}

void enviaMensagemParaJogador(const TCHAR* nome, const TCHAR* msg, const TCHAR* origem) {

    TCHAR buffer[BUFFER_MSG + 100];
    if (origem && origem[0] != TEXT('\0'))
        StringCchPrintf(buffer, BUFFER_MSG + 100, TEXT("[%s] %s"), origem, msg);
    else
        StringCchCopy(buffer, BUFFER_MSG + 100, msg);
    EnterCriticalSection(&csJogadores);
    for (int i = 0; i < numJogadores; i++) {
        if (_tcscmp(jogadores[i].nome, nome) == 0) {
            if (jogadores[i].hPipeJogador != INVALID_HANDLE_VALUE) {
                DWORD bytesWritten = 0;
                BOOL success = WriteFile(
                    jogadores[i].hPipeJogador,
                    buffer,
                    (lstrlen(buffer) + 1) * sizeof(TCHAR),
                    &bytesWritten,
                    NULL
                );
                if (!success) {
                    _tprintf(TEXT("Erro a enviar mensagem para o jogador %s.\n"), nome);
                }
            }
            break; // jogador encontrado, sai do ciclo
        }
    }

    LeaveCriticalSection(&csJogadores);
}


BOOL adicionaJogador(const TCHAR* nome) {
    BOOL resultado = FALSE;

    EnterCriticalSection(&csJogadores);

    if (numJogadores < MAX_JOGADORES) {
        // Criar pipe nomeado para este jogador receber mensagens do árbitro
        TCHAR pipeNome[BUFFER_MSG];
        StringCchPrintf(pipeNome, BUFFER_MSG, TEXT("%s%s"), PIPE_JOGADOR_BASE, nome);

        // Garante que pipe anterior (zombie) não existe
        HANDLE testPipe = CreateFile(
            pipeNome,
            GENERIC_READ | GENERIC_WRITE,
            0,
            NULL,
            OPEN_EXISTING,
            0,
            NULL);

        if (testPipe != INVALID_HANDLE_VALUE) {
            CloseHandle(testPipe);
            _tprintf(TEXT("Já existe pipe antigo para jogador %s. Eliminando referência antiga.\n"), nome);
            LeaveCriticalSection(&csJogadores); 
            return; // ou escolhe não criar jogador de novo
        }

        HANDLE hPipe = CreateNamedPipe(
            pipeNome,
            PIPE_ACCESS_OUTBOUND,   // só vai enviar mensagens para o jogador
            PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT,
            1,              // max instâncias
            BUFFER_MSG * sizeof(TCHAR), // out buffer
            BUFFER_MSG * sizeof(TCHAR), // in buffer (não usado aqui)
            0,
            NULL);

        if (hPipe == INVALID_HANDLE_VALUE) {
            _tprintf(TEXT("Erro a criar pipe para jogador %s. Código: %d\n"), nome, GetLastError());
            resultado = FALSE;
        }
        else {
            _tprintf(TEXT("Aguardando ligação do pipe do jogador %s...\n"), nome);
            BOOL connected = ConnectNamedPipe(hPipe, NULL) ?
                TRUE : (GetLastError() == ERROR_PIPE_CONNECTED);

            if (!connected) {
                CloseHandle(hPipe);
                _tprintf(TEXT("Falha ao conectar pipe do jogador %s.\n"), nome);
                resultado = FALSE;
            }
            else {
                _tcscpy_s(jogadores[numJogadores].nome, BUFFER_MSG, nome);
                jogadores[numJogadores].hPipeJogador = hPipe;
                numJogadores++;
                resultado = TRUE;
            }
        }
    }
    else {
        _tprintf(TEXT("Número máximo de jogadores atingido.\n"));
        resultado = FALSE;
    }

    LeaveCriticalSection(&csJogadores);

    return resultado;
}

void limpaJogadores() {
    EnterCriticalSection(&csJogadores);
    for (int i = 0; i < numJogadores; i++) {
        if (jogadores[i].hPipeJogador != INVALID_HANDLE_VALUE) {
            FlushFileBuffers(jogadores[i].hPipeJogador);
            DisconnectNamedPipe(jogadores[i].hPipeJogador);
            CloseHandle(jogadores[i].hPipeJogador);
        }
    }
    numJogadores = 0;
    LeaveCriticalSection(&csJogadores);
}

void encerraJogo() {
    // Notificar todos jogadores
    enviaMensagemParaTodos(TEXT("O jogo vai terminar. Obrigado por participarem!"), TEXT("[ARBITRO]"));

    // Mostrar jogador com mais pontos
    if (numJogadores > 0) {
        int idxMax = 0;
        for (int i = 1; i < numJogadores; i++) {
            if (jogadores[i].pontos > jogadores[idxMax].pontos) {
                idxMax = i;
            }
        }
        _tprintf(TEXT("Jogador com mais pontos: %s (%.2f pontos)\n"), jogadores[idxMax].nome, jogadores[idxMax].pontos);
    }
    else {
        _tprintf(TEXT("Nenhum jogador conectado.\n"));
    }

    enviaMensagemParaTodos(TEXT("ENCERRAR:"), NULL);
    // Limpar recursos e sair
    limpaJogadores();

    UnmapViewOfFile(shm);
    CloseHandle(hMapFile);
    TerminateThread(hThreadLetras, 0);
    CloseHandle(hThreadLetras);

    CloseHandle(hMutexLetras);
    CloseHandle(hEventoLetras);

    DeleteCriticalSection(&csLetras);

    DeleteCriticalSection(&csJogadores);
    ExitProcess(0);
}

void expulsaJogador(const TCHAR* nome) {
    int idx = -1;
    EnterCriticalSection(&csJogadores);
    for (int i = 0; i < numJogadores; i++) {
        if (_tcscmp(jogadores[i].nome, nome) == 0) {
            idx = i;
            break;
        }
    }
    if (idx != -1) {
        _tprintf(TEXT("Expulsar jogador %s...\n"), nome);
        // Notificar todos
        TCHAR msg[BUFFER_MSG];
        StringCchPrintf(msg, BUFFER_MSG, TEXT("Jogador %s foi expulso pelo árbitro."), nome);
        enviaMensagemParaTodos(msg, TEXT("ARBITRO"));

        StringCchPrintf(msg, BUFFER_MSG, TEXT("EXCLUIR:"));
        enviaMensagemParaJogador(nome, msg, NULL);

        fecharJogador(idx);
    }
    else {
        _tprintf(TEXT("Jogador %s não encontrado para expulsar.\n"), nome);
    }
    LeaveCriticalSection(&csJogadores);
}




void removeLetrasUsadas(const TCHAR* palavra) {

    WaitForSingleObject(hMutexLetras, INFINITE);
    TCHAR letra;
    int letrasDisponiveis[256] = { 0 };
    // Contar as letras na memória partilhada
    for (int i = 0; i < shm->max_letras; i++) {
        if (shm->letras[i] != TEXT('\0')) {
            letrasDisponiveis[(unsigned char)shm->letras[i]]++;
        }
    }
    // Para cada letra na palavra, remover uma ocorrência da memória partilhada
    for (int i = 0; palavra[i] != TEXT('\0'); i++) {
        letra = palavra[i];

        // Se ainda houver essa letra disponível para remover
        if (letrasDisponiveis[(unsigned char)letra] > 0) {
            // Procurar letra na memória partilhada e remover
            for (int j = 0; j < shm->max_letras; j++) {
                if (shm->letras[j] == letra) {
                    shm->letras[j] = TEXT('\0');
                    letrasDisponiveis[(unsigned char)letra]--;
                    break;
                }
            }
        }
    }

    // Compactar array de letras para eliminar buracos '\0'
    int writeIdx = 0;
    for (int readIdx = 0; readIdx < shm->max_letras; readIdx++) {
        if (shm->letras[readIdx] != TEXT('\0')) {
            if (writeIdx != readIdx) {
                shm->letras[writeIdx] = shm->letras[readIdx];
                shm->letras[readIdx] = TEXT('\0');
            }
            writeIdx++;
        }
    }
    ReleaseMutex(hMutexLetras);
}

void enviaPontuacaoJogador(const TCHAR* nome, Jogador jogadores[], int numJogadores) {
    for (int i = 0; i < numJogadores; i++) {
        if (_tcscmp(jogadores[i].nome, nome) == 0) {
            TCHAR resposta[BUFFER_MSG];
            StringCchPrintf(resposta, BUFFER_MSG, TEXT("Pontuação atual: %.2f ponto(s)."), jogadores[i].pontos);
            DWORD bytesWritten;
            WriteFile(jogadores[i].hPipeJogador, resposta, (lstrlen(resposta) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
            return;
        }
    }
}


void enviaListaJogadores(const TCHAR* nome, Jogador jogadores[], int numJogadores) {
    HANDLE pipeDestino = NULL;

    for (int i = 0; i < numJogadores; i++) {
        if (_tcscmp(jogadores[i].nome, nome) == 0) {
            pipeDestino = jogadores[i].hPipeJogador;
            break;
        }
    }

    if (!pipeDestino) return;

    TCHAR lista[BUFFER_MSG] = TEXT("Jogadores conectados: ");
    for (int i = 0; i < numJogadores; i++) {
        if (i > 0) _tcscat_s(lista, BUFFER_MSG, TEXT(", "));
        _tcscat_s(lista, BUFFER_MSG, jogadores[i].nome);
    }

    DWORD bytesWritten;
    WriteFile(pipeDestino, lista, (lstrlen(lista) + 1) * sizeof(TCHAR), &bytesWritten, NULL);
}

int obterClassificacaoJogador(int idx) {
    float pontosJogador = jogadores[idx].pontos;
    int classificacao = 0;

    for (int i = 0; i < numJogadores; i++) {
        if (i != idx && jogadores[i].pontos > pontosJogador)
            classificacao++;
    }

    return classificacao; // 0 = primeiro lugar
}

DWORD WINAPI geraLetrasPeriodicamente(LPVOID param) {
    srand((unsigned int)time(NULL) ^ GetCurrentThreadId());
    HANDLE hEvento = OpenEvent(EVENT_MODIFY_STATE, FALSE, EVENTO_LETRAS);
    if (hEvento == NULL) {
        _tprintf(TEXT("Erro ao abrir evento das letras no gerador. Código: %d\n"), GetLastError());
        return 1;
    }

    while (1) {
        Sleep(shm->ritmo * 1000);

        // Gerar nova letra aleatória (A-Z)
        TCHAR novaLetra = TEXT('A') + (rand() % 26);

        // Secção crítica: atualiza memória partilhada
        WaitForSingleObject(hMutexLetras, INFINITE); // opcional: usa mutex se quiseres proteger a memória

        int i;
        for (i = 0; i < shm->max_letras; i++) {
            if (shm->letras[i] == 0) {
                // Slot livre — inserir letra
                shm->letras[i] = novaLetra;
                break;
            }
        }

        if (i == shm->max_letras) {
            // Nenhum slot livre — remover primeira letra (mais antiga) e shift à esquerda
            for (int j = 0; j < shm->max_letras - 1; j++) {
                shm->letras[j] = shm->letras[j + 1];
            }
            shm->letras[shm->max_letras - 1] = novaLetra;
        }

        // Libertar acesso à memória partilhada
        ReleaseMutex(hMutexLetras); // se usares mutex

        // Sinalizar evento
        SetEvent(hEvento);
        ResetEvent(hEvento); // necessário para que os jogadores possam detetar nova ativação

        _tprintf(TEXT("[ARBITRO] Letra '%c' adicionada às letras visíveis.\n"), novaLetra);
    }

    CloseHandle(hEvento);
    return 0;
}

DWORD WINAPI threadAtendeJogador(LPVOID param) {
    HANDLE hPipeArbitro = (HANDLE)param;
    TCHAR buffer[BUFFER_MSG];
    DWORD bytesRead;

    while (TRUE) {
        BOOL success = ReadFile(hPipeArbitro, buffer, sizeof(buffer), &bytesRead, NULL);

        if (!success || bytesRead == 0) {
            break; // Conexão terminada
        }

        if (_tcsncmp(buffer, TEXT("NOME:"), 5) == 0) {
            EnterCriticalSection(&csLetras);
            if (!letrasIniciadas) {
                letrasIniciadas = TRUE;
                hThreadLetras = CreateThread(NULL, 0, geraLetrasPeriodicamente, NULL, 0, NULL);
                if (hThreadLetras == NULL) {
                    _tprintf(TEXT("Erro ao criar thread de letras. Código: %d\n"), GetLastError());
                }
                else {
                    _tprintf(TEXT("Thread de geração de letras iniciada após ligação do primeiro jogador.\n"));
                }
            }
            LeaveCriticalSection(&csLetras);
            TCHAR nomeJogador[BUFFER_MSG];
            _tcscpy_s(nomeJogador, BUFFER_MSG, buffer + 5);

            _tprintf(TEXT("Pedido de novo jogador: %s\n"), nomeJogador);

            if (adicionaJogador(nomeJogador)) {
                _tprintf(TEXT("Jogador %s adicionado.\n"), nomeJogador);
                enviaMensagemParaTodos(TEXT("[NOVO_JOGADOR]"), nomeJogador);
            }
            else {
                _tprintf(TEXT("Não foi possível adicionar jogador %s.\n"), nomeJogador);
            }

        }
        else if (_tcsncmp(buffer, TEXT("SAIR:"), 5) == 0) {
            TCHAR nomeJogador[BUFFER_MSG];
            _tcscpy_s(nomeJogador, BUFFER_MSG, buffer + 5);

            int idx = -1;

            EnterCriticalSection(&csJogadores);
            for (int i = 0; i < numJogadores; i++) {
                if (_tcscmp(jogadores[i].nome, nomeJogador) == 0) {
                    idx = i;
                    break;
                }
            }
            LeaveCriticalSection(&csJogadores);

            if (idx != -1) {
                _tprintf(TEXT("Jogador %s saiu.\n"), nomeJogador);
                enviaMensagemParaTodos(TEXT("[SAIU]"), nomeJogador);
                fecharJogador(idx);
            }

        }
        else if (_tcsncmp(buffer, TEXT("PONT:"), 5) == 0) {
            enviaPontuacaoJogador(buffer + 5, jogadores, numJogadores);
        }
        else if (_tcsncmp(buffer, TEXT("JOGS:"), 5) == 0) {
            enviaListaJogadores(buffer + 5, jogadores, numJogadores);
        }
        else if (_tcsncmp(buffer, TEXT("MSG:"), 4) == 0) {
            // O formato esperado é "MSG:<palavra>:<nomeJogador>"
            TCHAR* palavra = buffer + 4;

            // Procurar separador
            TCHAR* separador = _tcschr(palavra, TEXT(':'));
            if (separador) {
                *separador = TEXT('\0'); // termina palavra
                TCHAR* nomeJogador = separador + 1;

                int idx = obterIndiceJogador(nomeJogador);

                if (idx == -1) continue;

                int classificacaoAnterior = obterClassificacaoJogador(idx);

                TCHAR mensagem[BUFFER_MSG];

                if (palavraValida(palavra)) {
                    jogadores[idx].pontos++;
                    CharUpperBuff((LPTSTR)palavra, lstrlen(palavra));

                    removeLetrasUsadas(palavra);

                    // Opcional: sinalizar o evento para indicar reset
                    SetEvent(hEventoLetras);
                    ResetEvent(hEventoLetras);

                    StringCchPrintf(mensagem, BUFFER_MSG,
                        TEXT("%s acertou! Palavra válida."),
                        nomeJogador);

                    enviaMensagemParaTodos(mensagem, nomeJogador);

                    // Verificar se subiu na classificação
                    int novaClassificacao = obterClassificacaoJogador(idx);
                    if (novaClassificacao < classificacaoAnterior) {
                        StringCchPrintf(mensagem, BUFFER_MSG,
                            TEXT("%s passou para a posição #%d!"),
                            nomeJogador, novaClassificacao + 1);
                        enviaMensagemParaTodos(mensagem, TEXT("ARBITRO"));
                    }
                } else {
                    if (jogadores[idx].pontos >= 0.5)
                        jogadores[idx].pontos -= 0.5;
                    else
                        jogadores[idx].pontos = 0;
                    StringCchPrintf(mensagem, BUFFER_MSG,
                        TEXT("Tentou \"%s\", mas a palavra não é válida."),
                        palavra);
                    enviaMensagemParaJogador(nomeJogador, mensagem, TEXT("ARBITRO"));
                }
            }
        }
    }

    FlushFileBuffers(hPipeArbitro);
    DisconnectNamedPipe(hPipeArbitro);
    CloseHandle(hPipeArbitro);

    _tprintf(TEXT("Conexão de jogador terminada.\n"));

    return 0;
}



BOOL letrasDisponiveisParaPalavra(const TCHAR* palavra) {
    if (!palavra || !shm) return FALSE;

    // Criar array de contagem das letras da memória partilhada (A-Z)
    int letrasMemoria[26] = { 0 };
    WaitForSingleObject(hMutexLetras, INFINITE);
    for (int i = 0; i < shm->max_letras; i++) {
        TCHAR letra = shm->letras[i];
        if (letra >= TEXT('A') && letra <= TEXT('Z')) {
            letrasMemoria[letra - TEXT('A')]++;
        }
        else if (letra >= TEXT('a') && letra <= TEXT('z')) {
            letrasMemoria[letra - TEXT('a')]++;
        }
    }
    ReleaseMutex(hMutexLetras);

    // Contar letras da palavra
    int letrasPalavra[26] = { 0 };
    for (int i = 0; palavra[i] != TEXT('\0'); i++) {
        TCHAR c = palavra[i];
        if (c >= TEXT('A') && c <= TEXT('Z')) {
            letrasPalavra[c - TEXT('A')]++;
        }
        else if (c >= TEXT('a') && c <= TEXT('z')) {
            letrasPalavra[c - TEXT('a')]++;
        }
        else {
            // Caracter inválido para palavra (não alfabético)
            return FALSE;
        }
    }

    // Verificar se para cada letra da palavra temos no mínimo o mesmo número na memória
    for (int i = 0; i < 26; i++) {
        if (letrasPalavra[i] > letrasMemoria[i]) {
            return FALSE;
        }
    }

    return TRUE;
}


BOOL palavraValida(const TCHAR* palavra) {
    if (!palavra) return FALSE;

    // Verifica se as letras existem na memória partilhada
    if (!letrasDisponiveisParaPalavra(palavra)) {
        return FALSE;
    }

    // Verifica se a palavra existe no dicionário
    FILE* f = _tfopen(TEXT("dicionario.txt"), TEXT("r"));
    if (!f) return FALSE;

    TCHAR linha[BUFFER_MSG];
    while (_fgetts(linha, BUFFER_MSG, f)) {
        // Remover \n
        size_t len = _tcslen(linha);
        if (len > 0 && linha[len - 1] == TEXT('\n'))
            linha[len - 1] = TEXT('\0');

        if (_tcsicmp(linha, palavra) == 0) {
            fclose(f);
            return TRUE;
        }
    }

    fclose(f);
    return FALSE;
}

DWORD WINAPI threadComandosArbitro(LPVOID lpParam) {
    TCHAR comando[BUFFER_MSG];

    while (1) {
        _tprintf(TEXT("\n[Árbitro]> "));
        _fgetts(comando, BUFFER_MSG, stdin);

        // Remove newline
        comando[_tcslen(comando) - 1] = TEXT('\0');

        if (_tcscmp(comando, TEXT("listar")) == 0) {
            EnterCriticalSection(&csJogadores);
            if (numJogadores == 0) {
                _tprintf(TEXT("Nenhum jogador conectado.\n"));
            }
            else {
                _tprintf(TEXT("Lista de jogadores e suas pontuações:\n"));
                for (int i = 0; i < numJogadores; i++) {
                    _tprintf(TEXT("- %s: %.2f ponto(s)\n"), jogadores[i].nome, jogadores[i].pontos);
                }
            }
            LeaveCriticalSection(&csJogadores);
        }
        else if (_tcsncmp(comando, TEXT("iniciarbot "), 11) == 0) {
            TCHAR nomeBot[BUFFER_MSG];
            int tempoReacao;

            if (_stscanf_s(comando + 11, TEXT("%s %d"), nomeBot, (unsigned)_countof(nomeBot), &tempoReacao) != 2) {
                _tprintf(TEXT("Uso correto: iniciarbot <nome> <tempo_reacao>\n"));
                return 0;
            }

            TCHAR cmdLine[BUFFER_MSG];
            StringCchPrintf(cmdLine, BUFFER_MSG, TEXT("bot.exe %s %d"), nomeBot, tempoReacao);

            STARTUPINFO si = { sizeof(si) };
            PROCESS_INFORMATION pi;
            if (!CreateProcess(NULL, cmdLine, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi)) {
                _tprintf(TEXT("Erro ao iniciar bot %s. Código: %d\n"), nomeBot, GetLastError());
            }
            else {
                CloseHandle(pi.hThread);
                CloseHandle(pi.hProcess);
                _tprintf(TEXT("Bot %s iniciado com sucesso.\n"), nomeBot);
            }
        }
        else if (_tcsncmp(comando, TEXT("excluir "), 8) == 0) {
            TCHAR nomeParaExpulsar[BUFFER_MSG];
            _tcscpy_s(nomeParaExpulsar, BUFFER_MSG, comando + 8);
            expulsaJogador(nomeParaExpulsar);
        }
        else if (_tcscmp(comando, TEXT("acelerar")) == 0) {
            if (shm->ritmo > 1) shm->ritmo--;
            _tprintf(TEXT("Ritmo agora: %d s\n"), shm->ritmo);
        }
        else if (_tcscmp(comando, TEXT("travar")) == 0) {
            shm->ritmo++;
            _tprintf(TEXT("Ritmo agora: %d s\n"), shm->ritmo);
        }
        else if (_tcsicmp(comando, TEXT("encerrar")) == 0) {
            encerraJogo();
            break;
        }
        else {
            _tprintf(TEXT("Comando não reconhecido: %s\n"), comando);
        }
    }

    return 0;
}


int obterIndiceJogador(const TCHAR* nome) {
    for (int i = 0; i < numJogadores; i++) {
        if (_tcscmp(jogadores[i].nome, nome) == 0)
            return i;
    }
    if (numJogadores < MAX_JOGADORES) {
        _tcscpy_s(jogadores[numJogadores].nome, BUFFER_MSG, nome);
        jogadores[numJogadores].pontos = 0;
        return numJogadores++;
    }
    return -1; // Limite atingido
}




int _tmain(int argc, TCHAR* argv[]) {
#ifdef UNICODE
    _setmode(_fileno(stdin), _O_WTEXT);
    _setmode(_fileno(stdout), _O_WTEXT);
#endif

    InitializeCriticalSection(&csJogadores);
    InitializeCriticalSection(&csLetras);


    int max_letras = MAX_LETRAS; // valores por defeito
    int ritmo = INTERVALO_GERACAO_LETRAS;
    HKEY hKey;
    LONG result = RegCreateKeyEx(HKEY_CURRENT_USER, TEXT("Software\\TrabSO2"), 0, NULL, 0, KEY_READ | KEY_WRITE, NULL, &hKey, NULL);
    if (result == ERROR_SUCCESS) {
        DWORD size = sizeof(DWORD);
        DWORD type;
        DWORD val;

        if (RegQueryValueEx(hKey, TEXT("MAXLETRAS"), NULL, &type, (LPBYTE)&val, &size) == ERROR_SUCCESS && type == REG_DWORD)
            max_letras = min((int)val, 12); // limite superior
        else {
            val = max_letras;
            RegSetValueEx(hKey, TEXT("MAXLETRAS"), 0, REG_DWORD, (const BYTE*)&val, sizeof(DWORD));
        }

        size = sizeof(DWORD);
        if (RegQueryValueEx(hKey, TEXT("RITMO"), NULL, &type, (LPBYTE)&val, &size) == ERROR_SUCCESS && type == REG_DWORD)
            ritmo = (int)val;
        else {
            val = ritmo;
            RegSetValueEx(hKey, TEXT("RITMO"), 0, REG_DWORD, (const BYTE*)&val, sizeof(DWORD));
        }

        RegCloseKey(hKey);
        _tprintf(TEXT("Acesso ao registry com sucesso. Ritmo:%d MaxLetras:%d.\n"), ritmo, max_letras);
    }
    else {
        _tprintf(TEXT("Erro ao aceder ao registry. A usar valores por defeito.\n"));
    }

    // Abrir memória partilhada para letras
    hMapFile = CreateFileMapping(
        INVALID_HANDLE_VALUE,
        NULL,
        PAGE_READWRITE,
        0,
        sizeof(MemoriaPartilhada),
        SHM_NAME);
    if (!hMapFile) {
        _tprintf(TEXT("Erro ao criar memória partilhada. Código de erro: %lu\n"), GetLastError());
        DeleteCriticalSection(&csJogadores);
        return 1;
    }
    shm = (MemoriaPartilhada*)MapViewOfFile(hMapFile, FILE_MAP_ALL_ACCESS, 0, 0, 0);
    if (!shm) {
        _tprintf(TEXT("Erro ao mapear memória.\n"));
        CloseHandle(hMapFile);
        DeleteCriticalSection(&csJogadores);
        return 1;
    }
    shm->max_letras = max_letras;
    shm->ritmo = ritmo;
    memset(shm->letras, 0, sizeof(shm->letras));
    
    hMutexLetras = CreateMutex(NULL, FALSE, MUTEX_LETRAS);
    if (hMutexLetras == NULL) {
        _tprintf(TEXT("Erro a criar mutex das letras. Código: %lu\n"), GetLastError());
        UnmapViewOfFile(shm);
        CloseHandle(hMapFile);
        DeleteCriticalSection(&csJogadores);
        return 1;
    }

    hEventoLetras = CreateEvent(NULL, TRUE, FALSE, EVENTO_LETRAS);
    if (hEventoLetras == NULL) {
        _tprintf(TEXT("Erro a criar evento das letras. Código: %lu\n"), GetLastError());
        CloseHandle(hMutexLetras);
        UnmapViewOfFile(shm);
        CloseHandle(hMapFile);
        DeleteCriticalSection(&csJogadores);
        return 1;
    }

    HANDLE hThreadComandos = CreateThread(NULL, 0, threadComandosArbitro, NULL, 0, NULL);
    if (hThreadComandos == NULL) {
        CloseHandle(hMutexLetras);
        CloseHandle(hEventoLetras);
        UnmapViewOfFile(shm);
        CloseHandle(hMapFile);
        DeleteCriticalSection(&csJogadores);
        _tprintf(TEXT("Erro ao criar thread dos comandos do árbitro. Código: %d\n"), GetLastError());
    }


    _tprintf(TEXT("Aguarda ligações dos jogadores...\n"));

    // Inicializar letras a '\0'
    WaitForSingleObject(hMutexLetras, INFINITE);
    for (int i = 0; i < shm->max_letras; i++) {
        shm->letras[i] = TEXT('\0');
    }
    ReleaseMutex(hMutexLetras);




    while (TRUE) {
        HANDLE hPipeArbitro = CreateNamedPipe(
            PIPE_ARBITRO,
            PIPE_ACCESS_INBOUND, // só vai ler mensagens dos jogadores
            PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT,
            MAX_JOGADORES,
            BUFFER_MSG * sizeof(TCHAR),
            BUFFER_MSG * sizeof(TCHAR),
            0,
            NULL);

        if (hPipeArbitro == INVALID_HANDLE_VALUE) {
            _tprintf(TEXT("Erro ao criar pipe do árbitro. Código: %d\n"), GetLastError());
            break;
        }

        BOOL connected = ConnectNamedPipe(hPipeArbitro, NULL) ?
            TRUE : (GetLastError() == ERROR_PIPE_CONNECTED);

        if (connected) {
            DWORD threadId;
            HANDLE hThread = CreateThread(NULL, 0, threadAtendeJogador, hPipeArbitro, 0, &threadId);
            if (hThread == NULL) {
                _tprintf(TEXT("Erro ao criar thread para jogador. Código: %d\n"), GetLastError());
                CloseHandle(hPipeArbitro);
            }
            else {
                CloseHandle(hThread);  // thread continua a correr, não é necessário guardar handle
            }
        }
        else {
            CloseHandle(hPipeArbitro);
        }
    }

    limpaJogadores();

    UnmapViewOfFile(shm);
    CloseHandle(hMapFile);
    TerminateThread(hThreadLetras, 0);
    CloseHandle(hThreadLetras);

    CloseHandle(hMutexLetras);
    CloseHandle(hEventoLetras);

    DeleteCriticalSection(&csLetras);

    DeleteCriticalSection(&csJogadores);

    return 0;
}
