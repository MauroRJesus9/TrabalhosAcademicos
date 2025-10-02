#include <windows.h>
#include <tchar.h>
#include <strsafe.h>
#include "../utils.h"

#define ID_TIMER_UPDATE 1
#define ID_LISTBOX_JOGADORES 1001
#define ID_LABEL_LETRAS     1002
#define ID_LABEL_ULTIMA     1003
#define ID_MENU_SAIR        2001
#define ID_MENU_SOBRE       2002
#define ID_MENU_CONFIG      2003

HWND hListBoxJogadores, hStaticLetras, hStaticUltima;
MemoriaPartilhada* shm = NULL;
HANDLE hMapFile;
int maxJogadoresPainel = 5;

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
void AtualizaPainel();

void MostrarSobre(HWND hwnd) {
    MessageBox(hwnd, TEXT("Trabalho Prático SO2 2024/25\n"), TEXT("Sobre"), MB_OK | MB_ICONINFORMATION);
}

void ConfigurarJogadores(HWND parent) {
    HWND hDlg = CreateWindowEx(0, TEXT("STATIC"), TEXT("Configuração"),
        WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU,
        CW_USEDEFAULT, CW_USEDEFAULT, 300, 150,
        parent, NULL, NULL, NULL);

    CreateWindow(TEXT("STATIC"), TEXT("Máx. Jogadores a mostrar:"),
        WS_VISIBLE | WS_CHILD,
        10, 20, 200, 20,
        hDlg, NULL, NULL, NULL);

    HWND hEdit = CreateWindow(TEXT("EDIT"), TEXT(""),
        WS_VISIBLE | WS_CHILD | WS_BORDER | ES_NUMBER,
        10, 45, 260, 25,
        hDlg, (HMENU)1, NULL, NULL);

    CreateWindow(TEXT("BUTTON"), TEXT("OK"),
        WS_VISIBLE | WS_CHILD,
        40, 80, 80, 25,
        hDlg, (HMENU)2, NULL, NULL);

    CreateWindow(TEXT("BUTTON"), TEXT("Cancelar"),
        WS_VISIBLE | WS_CHILD,
        150, 80, 80, 25,
        hDlg, (HMENU)3, NULL, NULL);

    ShowWindow(hDlg, SW_SHOW);
    UpdateWindow(hDlg);

    // Loop modal simples
    MSG msg;
    BOOL done = FALSE;
    while (!done && GetMessage(&msg, NULL, 0, 0)) {
        if (msg.hwnd == hDlg || IsChild(hDlg, msg.hwnd)) {
            if (msg.message == WM_COMMAND) {
                switch (LOWORD(msg.wParam)) {
                case 2: { // OK
                    TCHAR temp[10];
                    GetWindowText(hEdit, temp, 10);
                    int val = _ttoi(temp);
                    if (val > 0 && val <= MAX_JOGADORES) {
                        maxJogadoresPainel = val;
                    }
                    done = TRUE;
                    DestroyWindow(hDlg);
                    break;
                }
                case 3: // Cancelar
                    done = TRUE;
                    DestroyWindow(hDlg);
                    break;
                }
            }
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
        else {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
    }
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance,
    LPSTR lpCmdLine, int nCmdShow) {

    WNDCLASS wc = { 0 };
    wc.lpfnWndProc = WndProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = TEXT("PainelClasse");
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    RegisterClass(&wc);

    HWND hwnd = CreateWindow(TEXT("PainelClasse"), TEXT("Painel do Jogo"),
        WS_OVERLAPPEDWINDOW, CW_USEDEFAULT, CW_USEDEFAULT, 500, 400,
        NULL, NULL, hInstance, NULL);

    ShowWindow(hwnd, nCmdShow);
    UpdateWindow(hwnd);

    // Abrir memória partilhada
    hMapFile = OpenFileMapping(FILE_MAP_READ, FALSE, SHM_NAME);
    if (!hMapFile) {
        MessageBox(NULL, TEXT("Erro: Memória partilhada não encontrada.\nInicie primeiro o árbitro."), TEXT("Erro"), MB_OK | MB_ICONERROR);
        return 1;
    }
    shm = (MemoriaPartilhada*)MapViewOfFile(hMapFile, FILE_MAP_READ, 0, 0, 0);
    if (!shm) {
        MessageBox(NULL, TEXT("Erro ao mapear a memória partilhada."), TEXT("Erro"), MB_OK | MB_ICONERROR);
        CloseHandle(hMapFile);
        return 1;
    }

    SetTimer(hwnd, ID_TIMER_UPDATE, 1000, NULL);

    MSG msg;
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    KillTimer(hwnd, ID_TIMER_UPDATE);
    UnmapViewOfFile(shm);
    CloseHandle(hMapFile);

    return (int)msg.wParam;
}

void AtualizaPainel() {
    if (!shm) return;

    // Atualizar letras
    TCHAR letras[BUFFER_MSG] = TEXT("");
    for (int i = 0; i < shm->max_letras; i++) {
        TCHAR c = shm->letras[i] ? shm->letras[i] : TEXT('_');
        TCHAR tmp[4];
        StringCchPrintf(tmp, 4, TEXT("%c "), c);
        _tcscat_s(letras, BUFFER_MSG, tmp);
    }
    SetWindowText(hStaticLetras, letras);

    // Atualizar última palavra
    //SetWindowText(hStaticUltima, shm->ultimaPalavra);

    // Atualizar lista de jogadores
    SendMessage(hListBoxJogadores, LB_RESETCONTENT, 0, 0);
    /*int n = min(shm->numJogadoresPainel, maxJogadoresPainel);
    for (int i = 0; i < n; i++) {
        TCHAR linha[BUFFER_MSG];
        StringCchPrintf(linha, BUFFER_MSG, TEXT("%d. %s - %.2f pontos"),
            i + 1, shm->jogadoresPainel[i].nome, shm->jogadoresPainel[i].pontos);
        SendMessage(hListBoxJogadores, LB_ADDSTRING, 0, (LPARAM)linha);
    }*/
}

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
    case WM_CREATE: {
        CreateWindow(TEXT("STATIC"), TEXT("Letras:"), WS_VISIBLE | WS_CHILD,
            20, 20, 80, 20, hwnd, NULL, NULL, NULL);
        hStaticLetras = CreateWindow(TEXT("STATIC"), TEXT(""), WS_VISIBLE | WS_CHILD | SS_CENTER,
            100, 20, 350, 20, hwnd, (HMENU)ID_LABEL_LETRAS, NULL, NULL);

        CreateWindow(TEXT("STATIC"), TEXT("Ultima Palavra:"), WS_VISIBLE | WS_CHILD,
            20, 60, 120, 20, hwnd, NULL, NULL, NULL);
        hStaticUltima = CreateWindow(TEXT("STATIC"), TEXT(""), WS_VISIBLE | WS_CHILD | SS_CENTER,
            140, 60, 300, 20, hwnd, (HMENU)ID_LABEL_ULTIMA, NULL, NULL);

        hListBoxJogadores = CreateWindow(TEXT("LISTBOX"), NULL,
            WS_VISIBLE | WS_CHILD | WS_BORDER | LBS_NOTIFY,
            20, 100, 440, 200, hwnd, (HMENU)ID_LISTBOX_JOGADORES,
            NULL, NULL);

        // Criar menu
        HMENU hMenu = CreateMenu();
        AppendMenu(hMenu, MF_STRING, ID_MENU_CONFIG, TEXT("Max Jogadores"));
        AppendMenu(hMenu, MF_STRING, ID_MENU_SOBRE, TEXT("Sobre"));
        AppendMenu(hMenu, MF_STRING, ID_MENU_SAIR, TEXT("Sair"));
        SetMenu(hwnd, hMenu);

        break;
    }
    case WM_TIMER:
        AtualizaPainel();
        break;

    case WM_COMMAND:
        switch (LOWORD(wParam)) {
        case ID_MENU_SAIR:
            PostQuitMessage(0);
            break;
        case ID_MENU_SOBRE:
            MostrarSobre(hwnd);
            break;
        case ID_MENU_CONFIG:
            ConfigurarJogadores(hwnd);
            break;
        }
        break;

    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    }
    return DefWindowProc(hwnd, msg, wParam, lParam);
}