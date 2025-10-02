#include "utils.h"

void inicia_manager(Manager *manager){
    manager->server_running =  true;

    for(int i = 0; i < MAX_TOPICOS; i++){
        strcpy(manager->topicos[i].nome, "");
        manager->topicos[i].msg_count = 0;   
        manager->topicos[i].trinco = false;
    }

    for(int i = 0; i < MAX_USERS; i++){
        strcpy(manager->users[i].username, ""); 
        manager->users[i].n_topicos = 0;
        for(int j = 0; j < MAX_TOPICOS; j++) {
            strcpy(manager->users[i].topicos_subscritos[j], "");
        }
    }

    manager->n_users = 0;
    manager->n_topicos = 0;

    pthread_mutex_init(&manager->trinco, NULL);
}

void trataSinal(int signo) {
    printf("\nServidor interrompido.\n");
    unlink(PIPE_CLIENT_TO_MANAGER);
    exit(EXIT_SUCCESS);
}

void save_msgs(Manager *manager) {
    char *filename = getenv("MSG_FICH");
    if (!filename) {
        fprintf(stderr, "Erro: variável de ambiente MSG_FICH não está definida.\n");
        return;
    }

    FILE *file = fopen(filename, "w");
    if (!file) {
        perror("Erro ao abrir o ficheiro.");
        return;
    }

    for (int i = 0; i < manager->n_topicos; i++) {
        Topico *topico = &manager->topicos[i];
        for (int j = 0; j < topico->msg_count; j++) {
            Mensagem *msg = &topico->mensagens[j];
            if (msg->tempo > 0) {
                fprintf(file, "%s %s %d %s\n", 
                        topico->nome, 
                        msg->username, 
                        msg->tempo, 
                        msg->buffer);
            }
        }
    }

    fclose(file);
    printf("Mensagens persistentes salvaguardadas em '%s'.\n", filename);
}

void encerra_clientes(Manager *manager){
    Mensagem info_msg;
    snprintf(info_msg.buffer, sizeof(info_msg.buffer), "O servidor vai encerrar...\n");
    strcpy(info_msg.tipo, "close");
    strcpy(info_msg.username, "Server");

    for (int i = 0; i < manager->n_users; i++) {
        char client_fifo[100];
        snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, manager->users[i].username);
        int fd_destino = open(client_fifo, O_WRONLY);
        if (fd_destino != -1) {
            write(fd_destino, &info_msg, sizeof(info_msg));
            close(fd_destino);
        }
    }
}

void encerra(Manager *manager) {
    manager->server_running = false;
    encerra_clientes(manager);
    save_msgs(manager);
    unlink(PIPE_CLIENT_TO_MANAGER);
    pthread_mutex_destroy(&manager->trinco);
    pthread_join(manager->admin_thread, NULL);
    //printf("join da admin\n");
    pthread_join(manager->timer_thread, NULL);
    //printf("join da timer\n");
    printf("Servidor encerrado.\n");
    printf("Todos os FIFOs foram removidos.\n");
    exit(0);
}

void lista_users(Manager *manager){
    printf("[%d] utilizadores ligados.\n",manager->n_users);
    if(manager->n_users != 0){
        for (int i = 0; i < manager->n_users; i++) {
            printf("- %s\n", manager->users[i].username);
        }
    }
}

bool user_existe(Manager *manager, const char *username){
    for (int i = 0; i < manager->n_users; i++) {
        if (strcmp(manager->users[i].username, username) == 0) {
            return true;
        }
    }
    return false;
}
bool topico_existe(Manager *manager, const char *topico){
    for (int i = 0; i < manager->n_topicos; i++) {
        if (strcmp(manager->topicos[i].nome, topico) == 0) {
            return true;
        }
    }
    return false;
}

void remover_user(Manager *manager, const char *username, bool haMensagem) {
    if(haMensagem){
        Mensagem info_msg;
        snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Foste expulso...\n");
        strcpy(info_msg.tipo, "close");
        strcpy(info_msg.username, "Server");

        char client_fifo[100];
        snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, username);
        int fd_destino = open(client_fifo, O_WRONLY);
        if (fd_destino != -1) {
            write(fd_destino, &info_msg, sizeof(info_msg));
            close(fd_destino);
        }
    }
    int indice;
    for (int i = 0; i < manager->n_users; i++) {
        if (strcmp(manager->users[i].username, username) == 0) {
            indice = i;
            break;
        }
    }
    strcpy(manager->users[indice].username, "");
    
    for (int i = indice; i < manager->n_users - 1; i++) {
        manager->users[i] = manager->users[i + 1];
    }

    strcpy(manager->users[manager->n_users - 1].username, "");

    manager->n_users--;


    Mensagem info_msg_removed;
    strcpy(info_msg_removed.tipo, "info");
    strcpy(info_msg_removed.username, "Server");
    snprintf(info_msg_removed.buffer, sizeof(info_msg_removed.buffer), "O utilizador %s foi removido do servidor.", username);

     for (int i = 0; i < manager->n_users; i++) {
        if (strcmp(manager->users[i].username, username) != 0) {
            char client_fifo[100];
            snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, manager->users[i].username);
            int fd_destino = open(client_fifo, O_WRONLY);
            if (fd_destino != -1) {
                write(fd_destino, &info_msg_removed, sizeof(info_msg_removed));
                close(fd_destino);
            }
            break;
        }
    }
    printf("O user [%s] foi removido do manager.\n",username);
}

int subscribe_topico(Manager *manager, const char *username, const char *topico, bool enviaMsg) {
    Mensagem info_msg;
    strcpy(info_msg.tipo, "info");
    strcpy(info_msg.username, "Server");

    bool existe = false;
    int indice_user = -1;

    //return  0 - Criado
    //return  1 - Subscrito
    //return -1 - Erro ja esta no topico
    //return -2 - limite atingido

    int output;

    for (int i = 0; i < manager->n_topicos; i++) {
        if (strcmp(manager->topicos[i].nome, topico) == 0) {
            existe = true;
            break;
        }
    }

    if (existe) {
        bool ja_subscrito = false;

        for (int i = 0; i < manager->n_users; i++) {
            if (strcmp(manager->users[i].username, username) == 0) {
                for (int j = 0; j < manager->users[i].n_topicos; j++) {
                    if (strcmp(manager->users[i].topicos_subscritos[j], topico) == 0) {
                        ja_subscrito = true;
                        break;
                    }
                }
                break;
            } 
        }

        if (ja_subscrito) {
            snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Erro. Já está subscrito a esse topico...");
            output = -1;
        } else {
            for (int i = 0; i < manager->n_users; i++) {
                if (strcmp(manager->users[i].username, username) == 0) {
                    indice_user = i;
                    strcpy(manager->users[indice_user].topicos_subscritos[manager->users[indice_user].n_topicos], topico);
                    manager->users[indice_user].n_topicos++;

                    char client_fifo[100];
                    snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, username);
                    
                    snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Topico subscrito!");
                    int fd_destino = open(client_fifo, O_WRONLY);
                    if (fd_destino != -1) {
                        if (write(fd_destino, &info_msg, sizeof(info_msg)) == -1) {
                            perror("Erro ao enviar info");
                        }
                        close(fd_destino);
                    }
                    
                    if (enviaMsg) {
                        int fd_destino = open(client_fifo, O_WRONLY);
                        if (fd_destino != -1) {
                            for (int k = 0; k < manager->topicos[i].msg_count; k++) {
                                Mensagem msg_a_enviar;
                                strcpy(msg_a_enviar.tipo, "info");
                                strcpy(msg_a_enviar.username, manager->topicos[i].mensagens[k].username);
                                strcpy(msg_a_enviar.buffer, manager->topicos[i].mensagens[k].buffer);
                                msg_a_enviar.tempo = manager->topicos[i].mensagens[k].tempo;

                                if (write(fd_destino, &msg_a_enviar, sizeof(msg_a_enviar)) == -1) {
                                    perror("Erro ao enviar mensagem persistente");
                                }
                            }
                            close(fd_destino);
                        }
                    }
                    break;
                }
            }
            //output = 1;
            return 1;
        }
    } else {
        //printf("n topicos : %d",manager->n_topicos);
        if (manager->n_topicos < MAX_TOPICOS) {
            strcpy(manager->topicos[manager->n_topicos].nome, topico);
            manager->topicos[manager->n_topicos].msg_count = 0;
            manager->topicos[manager->n_topicos].trinco = false;
            manager->n_topicos++;

            for (int i = 0; i < manager->n_users; i++) {
                if (strcmp(manager->users[i].username, username) == 0) {
                    indice_user = i;
                    strcpy(manager->users[indice_user].topicos_subscritos[manager->users[indice_user].n_topicos], topico);
                    manager->users[indice_user].n_topicos++;
                    break;
                }
            }

            snprintf(info_msg.buffer, sizeof(info_msg.buffer), "O tópico %s foi criado com sucesso.", topico);
            output = 0;
        } else {
            snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Erro... Limite de tópicos atingido.");
            output = -2;
        }
    }
    if(enviaMsg){
        char client_fifo[100];
        snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, username);
        int fd_destino = open(client_fifo, O_WRONLY);
        if (fd_destino != -1) {
            write(fd_destino, &info_msg, sizeof(info_msg));
            close(fd_destino);
        }
    }
    return output;
}

void lista_topicos(Manager *manager){
    printf("[%d] Tópico(s).\n",manager->n_topicos);
    if(manager->n_topicos != 0){
        for (int i = 0; i < manager->n_topicos; i++) {
            printf("- %s -- N Mensagens : %d \n", manager->topicos[i].nome,manager->topicos[i].msg_count);
        }
    }

}

void show(Manager *manager,const char *topico){
    if(manager->n_topicos != 0){
        for (int i = 0; i < manager->n_topicos; i++) {
            if (strcmp(manager->topicos[i].nome, topico) == 0) {
                printf("[%d] Mensagem(s) no tópico:\n", manager->topicos[i].msg_count);
                for (int j = 0; j < manager->topicos[i].msg_count; j++) {
                    printf("Mensagem : %s\n", manager->topicos[i].mensagens[j].buffer);
                }
                return;
            }
        }
    }

}


int lock_topicos(Manager *manager,const char *topico){
    for (int i = 0; i < manager->n_topicos; i++) {
        if (strcmp(manager->topicos[i].nome, topico) == 0) {
            if(manager->topicos[i].trinco){
                printf("O tópico [%s] já se encontra bloqueado.\n",topico);
                return 0;
            }
            manager->topicos[i].trinco = true;
            printf("O tópico [%s] foi bloqueado.\n",topico);
            
            Mensagem info_msg;
            strcpy(info_msg.tipo, "info");
            strcpy(info_msg.username, "Server");
            snprintf(info_msg.buffer, sizeof(info_msg.buffer), "O tópico [%s] foi bloqueado.\n",topico);

            char user_atual[50];
            for (int i = 0; i < manager->n_users; i++) {
                strcpy(user_atual, manager->users[i].username);
                for (int j = 0; j < manager->users[i].n_topicos; j++) {
                    if (strcmp(manager->users[i].topicos_subscritos[j], topico) == 0) {
                        char client_fifo[100];
                        snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, user_atual);
                        int fd_destino = open(client_fifo, O_WRONLY);
                        if (fd_destino != -1) {
                            write(fd_destino, &info_msg, sizeof(info_msg));
                            close(fd_destino);
                        }
                        break;
                    }
                }
            }

            return 1;
        }
    }
    printf("O tópico [%s] não existe.\n",topico);
    return 0;
}
int unlock_topicos(Manager *manager,const char *topico){
    for (int i = 0; i < manager->n_topicos; i++) {
        if (strcmp(manager->topicos[i].nome, topico) == 0) {
            if(!manager->topicos[i].trinco){
                printf("O tópico [%s] já se encontra desbloqueado.\n",topico);
                return 0;
            }

            manager->topicos[i].trinco = false;
            printf("O tópico [%s] foi desbloqueado.\n",topico);

            Mensagem info_msg;
            strcpy(info_msg.tipo, "info");
            strcpy(info_msg.username, "Server");
            snprintf(info_msg.buffer, sizeof(info_msg.buffer), "O tópico [%s] foi desbloqueado.\n",topico);

            char user_atual[50];
            for (int i = 0; i < manager->n_users; i++) {
                strcpy(user_atual, manager->users[i].username);
                for (int j = 0; j < manager->users[i].n_topicos; j++) {
                    if (strcmp(manager->users[i].topicos_subscritos[j], topico) == 0) {
                        char client_fifo[100];
                        snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, user_atual);
                        int fd_destino = open(client_fifo, O_WRONLY);
                        if (fd_destino != -1) {
                            write(fd_destino, &info_msg, sizeof(info_msg));
                            close(fd_destino);
                        }
                        break;
                    }
                }
            }

            return 1;
        }
    }
    printf("O tópico [%s] não existe.\n",topico);
    return 0;
}

int unsubscribe_topico(Manager *manager, const char *username, const char *topico) {
    Mensagem info_msg;
    strcpy(info_msg.tipo, "info");
    strcpy(info_msg.username, "Server");

    bool existe = false;

    // Retornar 0 - Unsubscribe
    // Retornar 1 - Unsubscribe e eliminado
    // Retornar -1 - Erro
    int output;

    for (int i = 0; i < manager->n_topicos; i++) {
        if (strcmp(manager->topicos[i].nome, topico) == 0) {
            existe = true;
            break;
        }
    }

    if (existe) {
        bool ja_subscrito = false;

        for (int i = 0; i < manager->n_users; i++) {
            if (strcmp(manager->users[i].username, username) == 0) {
                for (int j = 0; j < manager->users[i].n_topicos; j++) {
                    if (strcmp(manager->users[i].topicos_subscritos[j], topico) == 0) {
                        ja_subscrito = true;
                        break;
                    }
                }
                break;
            }
        }

        if (ja_subscrito) {
            for (int i = 0; i < manager->n_users; i++) {
                if (strcmp(manager->users[i].username, username) == 0) {
                    int indice_user = i;
                    for (int j = 0; j < manager->users[indice_user].n_topicos; j++) {
                        if (strcmp(manager->users[indice_user].topicos_subscritos[j], topico) == 0) {
                            for (int k = j; k < manager->users[indice_user].n_topicos - 1; k++) {
                                strcpy(manager->users[indice_user].topicos_subscritos[k], manager->users[indice_user].topicos_subscritos[k + 1]);
                            }
                            manager->users[indice_user].n_topicos--;
                            break;
                        }
                    }
                    break;
                }
            }           

            bool ainda_subscrito = false;
            for (int i = 0; i < manager->n_users; i++) {
                for (int j = 0; j < manager->users[i].n_topicos; j++) {
                    if (strcmp(manager->users[i].topicos_subscritos[j], topico) == 0) {
                        ainda_subscrito = true;
                        break;
                    }
                }
                if (ainda_subscrito) break;
            }
            bool sem_mensagens=false;
            for (int i = 0; i < manager->n_topicos; i++) {
                if (strcmp(manager->topicos[i].nome, topico) == 0) {
                    if (manager->topicos[i].msg_count > 0) {
                        sem_mensagens = true; 
                    }
                    break;
                }
            }
                
            if (!ainda_subscrito && !sem_mensagens) {
                for (int i = 0; i < manager->n_topicos; i++) {
                    if (strcmp(manager->topicos[i].nome, topico) == 0) {
                        for (int j = i; j < manager->n_topicos - 1; j++) {
                            strcpy(manager->topicos[j].nome, manager->topicos[j + 1].nome);
                        }
                        strcpy(manager->topicos[manager->n_topicos - 1].nome, "");
                        manager->n_topicos--;
                        break;
                    }
                }
                snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Tópico apagado. Mais ninguém está subscrito.");
                output = 1;  // Unsubscribe e eliminado
            } else {
                snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Já não se encontra subscrito ao tópico");
                output = 0;  // Apenas unsubscribe
            }
        } else {
            snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Erro. Não está subscrito a esse tópico");
            output = -1;  // Erro
        }
    } else {
        snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Erro. Esse tópico não existe.");
        output = -1;  // Erro
    }

    char client_fifo[100];
    snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, username);
    int fd_destino = open(client_fifo, O_WRONLY);
    if (fd_destino != -1) {
        write(fd_destino, &info_msg, sizeof(info_msg));
        close(fd_destino);
    }
    return output;
}

void envia_topicos(Manager *manager, const char *username){
    Mensagem info_msg;
    strcpy(info_msg.tipo, "topicos");
    strcpy(info_msg.username, "Server");
    strcpy(info_msg.buffer, "");
    
    for (int i = 0; i < manager->n_users; i++) {
        if (strcmp(manager->users[i].username, username) == 0) {
            for (int j = 0; j < manager->users[i].n_topicos; j++) {
                if (j > 0) {
                    snprintf(info_msg.buffer + strlen(info_msg.buffer), sizeof(info_msg.buffer) - strlen(info_msg.buffer), ", ");
                }
                snprintf(info_msg.buffer + strlen(info_msg.buffer), sizeof(info_msg.buffer) - strlen(info_msg.buffer), "%s", manager->users[i].topicos_subscritos[j]);
            }
            break;
        }
    }

    char client_fifo[100];
    snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, username);
    int fd_destino = open(client_fifo, O_WRONLY);
    if (fd_destino != -1) {
        write(fd_destino, &info_msg, sizeof(info_msg));
        close(fd_destino);
    }
}

void envia_mensagem(Manager *manager, Mensagem *msg) {
    Mensagem msg_a_enviar;
    strcpy(msg_a_enviar.tipo, "msg");
    strcpy(msg_a_enviar.username, msg->username);
    strcpy(msg_a_enviar.buffer, msg->buffer);
    strcpy(msg_a_enviar.topico, msg->topico);
    msg_a_enviar.tempo = msg->tempo;

    // Verificar se o tópico já existe
    bool topico_existe = false;
    bool topico_bloqueado = false;
    int indice_topico = -1;
    
    for (int i = 0; i < manager->n_topicos; i++) {
        if (strcmp(manager->topicos[i].nome, msg->topico) == 0) {
            topico_existe = true;
            topico_bloqueado = manager->topicos[i].trinco;
            indice_topico = i;
            break;
        }
    }

    // Se o tópico não existe, criar o tópico
    if (!topico_existe && manager->n_topicos < MAX_TOPICOS) {
        strcpy(manager->topicos[manager->n_topicos].nome, msg->topico);
        manager->topicos[manager->n_topicos].msg_count = 0;
        manager->topicos[manager->n_topicos].trinco = false;
        indice_topico = manager->n_topicos;
        manager->n_topicos++;
        printf("Novo tópico criado: %s\n", msg->topico);
        for (int i = 0; i < manager->n_users; i++) {
            if (strcmp(manager->users[i].username, msg->username) == 0) {
                strcpy(manager->users[i].topicos_subscritos[manager->users[i].n_topicos], msg->topico);
                manager->users[i].n_topicos++;
                break;
            }
        }
    }
    if(topico_bloqueado) {
        snprintf(msg_a_enviar.buffer, sizeof(msg_a_enviar.buffer), "O tópico %s encontra-se bloqueado, mensagem não enviada", msg->topico);
        strcpy(msg_a_enviar.tipo, "info");
        strcpy(msg_a_enviar.username, "Servidor");

        char client_fifo[100];
        snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, msg->username);
        int fd_destino = open(client_fifo, O_WRONLY);
        if (fd_destino != -1) {
            write(fd_destino, &msg_a_enviar, sizeof(msg_a_enviar));
            close(fd_destino);
        }
        return;
    }
    
    if (msg->tempo > 0) {
        if (manager->topicos[indice_topico].msg_count < MAX_MENSAGENS_POR_TOPICO) {
            manager->topicos[indice_topico].mensagens[manager->topicos[indice_topico].msg_count] = *msg;
            manager->topicos[indice_topico].mensagens[manager->topicos[indice_topico].msg_count].tempo = msg->tempo;
            manager->topicos[indice_topico].msg_count++;
            printf("Mensagem persistente adicionada ao tópico: %s\n", msg->topico);
            printf("Tempo de vida da mensagem: %d segundos\n", msg->tempo);
        } else {
            snprintf(msg_a_enviar.buffer, sizeof(msg_a_enviar.buffer), "Erro: Limite de mensagens persistentes atingido para o tópico %s.\n", msg->topico);
            strcpy(msg_a_enviar.tipo, "info");
            strcpy(msg_a_enviar.username, "Servidor");
            printf("Limite de mensagens persistentes atingido para o tópico %s.\n", msg->topico);

            char client_fifo[100];
            snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, msg->username);
            int fd_destino = open(client_fifo, O_WRONLY);
            if (fd_destino != -1) {
                write(fd_destino, &msg_a_enviar, sizeof(msg_a_enviar));
                close(fd_destino);
            }
        }
    }

    char user_atual[50];

    for (int i = 0; i < manager->n_users; i++) {
        strcpy(user_atual, manager->users[i].username);
    
        if (strcmp(user_atual, msg->username) != 0) {
            for (int j = 0; j < manager->users[i].n_topicos; j++) {
                if (strcmp(manager->users[i].topicos_subscritos[j], msg->topico) == 0) {
                    char client_fifo[100];
                    snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, user_atual);
                    int fd_destino = open(client_fifo, O_WRONLY);
                    if (fd_destino != -1) {
                        write(fd_destino, &msg_a_enviar, sizeof(msg_a_enviar));
                        close(fd_destino);
                    }
                    break;
                }
            }
        }
    }

    printf("[%s] enviou uma mensagem para o tópico [%s]\n", msg->username, msg->topico);
}

void get_msgs(Manager *manager) {
    char *filename = getenv("MSG_FICH");
    if (!filename) {
        fprintf(stderr, "Erro: variável de ambiente MSG_FICH não está definida.\n");
        return;
    }

    FILE *file = fopen(filename, "r");
    if (!file) {
        perror("Erro ao abrir o ficheiro.");
        return;
    }

    char topico_nome[TAMANHO_NOME_TOPICO];
    char username[TAMANHO_NOME_USER];
    int tempo;
    char buffer[TAMANHO_MAX_MENSAGEM];

    while (fscanf(file, "%s %s %d %[^\n]", topico_nome, username, &tempo, buffer) == 4) {
        Topico *topico = NULL;

        // Verificar se o tópico já existe
        for (int i = 0; i < manager->n_topicos; i++) {
            if (strcmp(manager->topicos[i].nome, topico_nome) == 0) {
                topico = &manager->topicos[i];
                break;
            }
        }

        // Se não existir, criar um novo
        if (!topico && manager->n_topicos < MAX_TOPICOS) {
            topico = &manager->topicos[manager->n_topicos++];
            strcpy(topico->nome, topico_nome);
            topico->msg_count = 0;
        }

        if (topico && topico->msg_count < MAX_MENSAGENS_POR_TOPICO) {
            Mensagem *msg = &topico->mensagens[topico->msg_count++];
            strcpy(msg->username, username);
            msg->tempo = tempo;
            strcpy(msg->buffer, buffer);
        }
    }

    fclose(file);
    printf("Mensagens persistentes recuperadas de '%s'.\n", filename);
}


void *trataAdmin(void *arg) {
    Manager *manager = (Manager *)arg;
    char comando[100];
    printf("Comandos:\nusers | remove <username> | topics | show <topico> | lock <topico> | unlock <topico> | close");
    divisoria();
    while (manager->server_running) {
        fgets(comando, sizeof(comando), stdin);
        comando[strcspn(comando, "\n")] = 0;

        pthread_mutex_lock(&manager->trinco);

        if (strcmp(comando, "users") == 0) {
            
            lista_users(manager);

        } else if (strcmp(comando, "close") == 0) {

            printf("A desligar o servidor...\n");
            encerra(manager);

        } else if (strncmp(comando, "lock ", 5) == 0){
            char topico[TAMANHO_NOME_TOPICO];
            if (sscanf(comando, "lock %s", topico) == 1) {
                if(topico_existe(manager,topico)){
                    lock_topicos(manager, topico);
                }
                else {
                    printf("Erro. Esse topico não existe.\n");
                }
            } else {
                printf("Erro. Formato esperado: lock <topico>\n");
            }

        } else if (strncmp(comando, "unlock ", 7) == 0) {
            char topico[TAMANHO_NOME_TOPICO];
            if (sscanf(comando, "unlock %s", topico) == 1) {
                if(topico_existe(manager,topico)){
                    unlock_topicos(manager, topico);
                }
                else {
                    printf("Erro. Esse topico não existe.\n");
                }
            } else {
                printf("Erro. Formato esperado: unlock <topico>\n");
            }

        } else if (strncmp(comando, "remove ", 7) == 0) {
            char username[TAMANHO_NOME_USER];
            if (sscanf(comando, "remove %s", username) == 1) {
                if(user_existe(manager,username)){
                    remover_user(manager, username,true); //true pq há mensagem
                }
                else {
                    printf("Erro. Esse user não existe.\n");
                }
            } else {
                printf("Erro. Formato esperado: remove <username>\n");
            }
        } else if (strcmp(comando, "topics") == 0) {
            lista_topicos(manager);
        } else if (strncmp(comando, "show ", 5) == 0){
            char topico[TAMANHO_NOME_TOPICO];
            if (sscanf(comando, "show %s", topico) == 1) {
                show(manager,topico);
            } else {
                printf("Erro. Formato esperado: show <topico>\n");
            }
        }else {
            printf("Comando desconhecido: %s\n", comando);
        }

        divisoria();
        pthread_mutex_unlock(&manager->trinco);
    }
    return NULL;
}

void* timer(void *arg) {
    Manager *manager = (Manager *)arg;
    while (manager->server_running) {
        sleep(1);
        for (int i = 0; i < manager->n_topicos; i++) {
            for (int j = 0; j < manager->topicos[i].msg_count; j++) {
                if (manager->topicos[i].mensagens[j].tempo > 0) {
                    manager->topicos[i].mensagens[j].tempo--;
                    //printf("Tempo de vida restante para a mensagem do tópico %s: %d segundos\n", manager->topicos[i].nome, manager->topicos[i].mensagens[j].tempo);

                    if (manager->topicos[i].mensagens[j].tempo == 0) {
                        printf("Mensagem do tópico %s expirou e será removida.\n", manager->topicos[i].nome);
                        for (int k = j; k < manager->topicos[i].msg_count - 1; k++) {
                            manager->topicos[i].mensagens[k] = manager->topicos[i].mensagens[k + 1];
                        }
                        manager->topicos[i].msg_count--;
                        j--;
                    }
                }
            }
        }
    }

    return NULL;
}

void *trataCliente(void *arg) {
    ThreadData *data = (ThreadData *)arg;
    Manager *manager = data->manager;
    Mensagem msg = data->msg;

    pthread_mutex_lock(&manager->trinco);
    if (strcmp(msg.tipo, "login") == 0) {
        if (manager->n_users < MAX_USERS) {
            strcpy(manager->users[manager->n_users].username, msg.username);
            manager->n_users++;

            Mensagem info_msg;
            snprintf(info_msg.buffer, sizeof(info_msg.buffer), "Bem-vindo, %s", msg.username);
            strcpy(info_msg.tipo, "info");
            strcpy(info_msg.username, "Server");

            int fd_destino = open(data->client_fifo, O_WRONLY);
            if (fd_destino != -1) {
                write(fd_destino, &info_msg, sizeof(info_msg));
                close(fd_destino);
            }
            printf("Novo utilizador conectado: %s", msg.username);
        } else {
            Mensagem erro_msg;
            strcpy(erro_msg.tipo, "erro");
            strcpy(erro_msg.username, "Server");
            strcpy(erro_msg.buffer, "Erro: Limite de utilizadores atingido.");

            int fd_destino = open(data->client_fifo, O_WRONLY);
            if (fd_destino != -1) {
                write(fd_destino, &erro_msg, sizeof(erro_msg));
                close(fd_destino);
            }
        }
    }
    else if (strcmp(msg.tipo, "exit") == 0) {
        printf("O user [%s] desconectou-se.\n", msg.username);
        remover_user(manager,msg.username,false); //false pq não há mensagem
    }
    else if (strcmp(msg.tipo, "subscribe") == 0) {
        int result = subscribe_topico(manager, msg.username, msg.buffer,true); //true para enviar msg ao cliente
        if (result == 0) {
            printf("[%s] criou o tópico '%s'.", msg.username, msg.buffer);
        } else if (result == 1) {
            printf("[%s] subscreveu o tópico '%s'.", msg.username, msg.buffer);
        }
    }
    else if (strcmp(msg.tipo, "unsubscribe") == 0) {
        int result = unsubscribe_topico(manager, msg.username, msg.buffer);
        if (result == 0) {
            printf("[%s] já não está subscrito no tópico '%s'.", msg.username, msg.buffer);
        } else if (result == 1) {
            printf("[%s] o tópico '%s' foi removido.", msg.username, msg.buffer);
        } else if (result == -1) {
            printf("[%s] Erro :'%s'.", msg.username, msg.buffer);
        }
        // printf("\n topicos do manager depois do unsubscribe\n");
        // for(int i = 0; i < MAX_TOPICOS; i++){
        //     printf("%s ,",manager->topicos[i].nome);
        // }
    }
    else if (strcmp(msg.tipo, "topics") == 0) {
        envia_topicos(manager,msg.username);
    }
    else if (strcmp(msg.tipo, "msg") == 0) {
        envia_mensagem(manager,&msg);
    }
    else {

    }
    divisoria();
    pthread_mutex_unlock(&manager->trinco);
    return NULL;
}

int main() {
    char *filename = getenv("MSG_FICH");
    if (!filename) {
        fprintf(stderr, "Erro: variável de ambiente MSG_FICH não está definida. Encerrando...\n");
        return EXIT_FAILURE;
    }

    Manager manager;
    inicia_manager(&manager);
    printf("Manager inicializado.\n");

    get_msgs(&manager);
    printf("Mensagens carregadas.\n");

    if (access(PIPE_CLIENT_TO_MANAGER, F_OK) != -1) {
        perror("FIFO já existe");
        return EXIT_FAILURE;
    }
    if (cria_fifo(PIPE_CLIENT_TO_MANAGER) == -1) {
        perror("Erro ao criar FIFO global");
        return EXIT_FAILURE;
    }
    printf("FIFO global criado com sucesso.\n");

    printf("Servidor ligado.\n");

    signal(SIGINT, trataSinal);

    pthread_create(&manager.timer_thread, NULL, timer, &manager);
    //printf("Thread de temporizador criada.\n");

    pthread_create(&manager.admin_thread, NULL, trataAdmin, &manager);
    //printf("Thread de administração criada.\n");

    int global_fd = open(PIPE_CLIENT_TO_MANAGER, O_RDONLY);
    if (global_fd == -1) {
        perror("Erro ao abrir pipe global");
        return EXIT_FAILURE;
    }
    printf("Pipe global aberto com sucesso.\n");

    while (1) {
        Mensagem msg;
        if (read(global_fd, &msg, sizeof(msg)) <= 0) continue;

        ThreadData td;
        td.msg = msg;
        td.manager = &manager;
        snprintf(td.client_fifo, sizeof(td.client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, msg.username);

        pthread_t client_thread;
        pthread_create(&client_thread, NULL, trataCliente, &td);
        //printf("Thread para o cliente %s criada.\n", msg.username);
        pthread_join(client_thread,NULL);
        //printf("Thread para o cliente %s fechada.\n", msg.username);
    }

    pthread_join(manager.admin_thread, NULL);
    //printf("join da admin");
    pthread_join(manager.timer_thread, NULL);
    //printf("join do timer");

    close(global_fd);
    //printf("Pipe global fechado.\n");
    printf("Recursos do manager terminados.\n");
    encerra(&manager);
    return 0;
}