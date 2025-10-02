#include "utils.h"

int cria_fifo(const char *path) {
    if (access(path, F_OK) == 0) {
        if (unlink(path) == -1) {
            perror("Erro ao remover FIFO existente");
            return -1;
        }
    }
    if (mkfifo(path, 0666) == -1) {
        perror("Erro ao criar FIFO");
        return -1;
    }
    
    return 0;
}

void write_to_manager(const Mensagem *msg) {
    int fd_global = open(PIPE_CLIENT_TO_MANAGER, O_WRONLY);
    if (fd_global == -1) {
        perror("Erro ao abrir o pipe global para enviar mensagem");
        return;
    }
    write(fd_global, msg, sizeof(Mensagem));
    close(fd_global);
}

void divisoria(){
    //printf("\n--------------------------------------------------------------------------------------------\n");
    printf("\n\n");
}
void divisoriaCliente(){
    //printf("\n----------------------------------------------------------------------------------\n");
    printf("\n\n");
}