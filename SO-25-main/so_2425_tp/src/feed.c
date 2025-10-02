#include "utils.h"

void* capture_commands(void* arg) {
    Feed* feed = (Feed*)arg;
    char command[300];
    
    printf("Comandos:\ntopics | msg <topico> <duração> <mensagem> | subscribe/unsubscribe <topico> | exit");
    divisoriaCliente();
    while (1) {
        fgets(command, sizeof(command), stdin);
        command[strcspn(command, "\n")] = 0;

        Mensagem msg;
        strcpy(msg.username, feed->username);

        if (strcmp(command, "exit") == 0) {
            strcpy(msg.tipo, "exit");
            write_to_manager(&msg);
            printf("A sair...\n");
            unlink(feed->client_fifo);
            exit(0);
            break;
        } else if (strcmp(command, "topics") == 0) {
            strcpy(msg.tipo, "topics");
            write_to_manager(&msg);
        } else if (strncmp(command, "msg ", 4) == 0) {
            char topic[TAMANHO_NOME_TOPICO];
            int duration;
            char message[TAMANHO_MAX_MENSAGEM];

            if (sscanf(command, "msg %s %d %[^\n]", topic, &duration, message) == 3) {
                strcpy(msg.tipo, "msg");
                snprintf(msg.buffer, sizeof(msg.buffer), "%s", message);
                snprintf(msg.topico, sizeof(msg.topico), "%s", topic);
                msg.tempo = duration;
                write_to_manager(&msg);
            } else {
                printf("Comando msg inválido. Formato esperado: msg <tópico> <duração> <mensagem>\n");
            }
        } else if (strncmp(command, "subscribe ", 10) == 0) {
            char topic[TAMANHO_NOME_TOPICO];
            if (sscanf(command, "subscribe %s", topic) == 1) {
                strcpy(msg.tipo, "subscribe");
                snprintf(msg.buffer, sizeof(msg.buffer), "%s", topic);
                write_to_manager(&msg);
            } else {
                printf("Comando subscribe inválido. Formato esperado: subscribe <tópico>\n");
            }
        } else if (strncmp(command, "unsubscribe ", 11) == 0) {
            char topic[TAMANHO_NOME_TOPICO];
            if (sscanf(command, "unsubscribe %s", topic) == 1) {
                strcpy(msg.tipo, "unsubscribe");
                snprintf(msg.buffer, sizeof(msg.buffer), "%s", topic);
                write_to_manager(&msg);
            } else {
                printf("Comando unsubcribe inválido. Formato esperado: unsubscribe <tópico>\n");
            }
        } else {
            printf("Comando não reconhecido.\n");
        }
    }
    divisoriaCliente();
    return NULL;
}

void* receive_messages(void* arg) {
    Feed* feed = (Feed*)arg;
    int pipe_fd = open(feed->client_fifo, O_RDONLY);
    if (pipe_fd == -1) {
        perror("Erro ao abrir o FIFO exclusivo do cliente");
        return NULL;
    }

    Mensagem msg;
    while (1) {
        ssize_t bytesRead = read(pipe_fd, &msg, sizeof(msg));
        if (bytesRead > 0) {
            divisoriaCliente();
            if (strcmp(msg.tipo, "close") == 0){
                printf("[%s] - %s",msg.username,msg.buffer);
                unlink(feed->client_fifo);
                exit(0);
                break;
            }
            else if (strcmp(msg.tipo, "info") == 0){
                printf("[%s] - %s",msg.username,msg.buffer);
            }
            else if (strcmp(msg.tipo, "topicos") == 0){
                printf("Topicos:\n- %s",msg.buffer);
            }
            else if (strcmp(msg.tipo, "msg") == 0){
                printf("[%s] | [%s] - %s",msg.topico,msg.username,msg.buffer);
            }
            else if (strcmp(msg.tipo, "remove") == 0){
                printf("[%s] - %s [%s]",msg.username,msg.buffer,msg.topico);
            }
            divisoriaCliente();
        }
        else if (bytesRead == 0){
            continue;
        }
        else {
            perror("Erro ao ler do FIFO exclusivo");
            break;
        }
    }
    close(pipe_fd);
    return NULL;
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Uso: %s <username>\n", argv[0]);
        return EXIT_FAILURE;
    }

    Feed feed;
    strcpy(feed.username, argv[1]);

    char client_fifo[100];
    snprintf(client_fifo, sizeof(client_fifo), "%s%s", PIPE_MANAGER_TO_CLIENT, feed.username);
    strcpy(feed.client_fifo, client_fifo);
    
    if (mkfifo(client_fifo, 0666) == -1) {
        perror("Erro ao criar o FIFO exclusivo do cliente");
        return 0;
    }

    Mensagem login_msg;
    strcpy(login_msg.tipo, "login");
    strcpy(login_msg.username, feed.username);
    write_to_manager(&login_msg);

    int pipe_fd = open(client_fifo, O_RDONLY);
    if (pipe_fd == -1) {
        perror("Erro ao abrir FIFO exclusivo para leitura inicial");
        return EXIT_FAILURE;
    }

    Mensagem response;
    if (read(pipe_fd, &response, sizeof(response)) > 0) {
        if (strcmp(response.tipo, "erro") == 0) {
            printf("Erro do servidor: %s\n", response.buffer);
            close(pipe_fd);
            return EXIT_FAILURE;
        } else if (strcmp(response.tipo, "info") == 0) {
            printf("%s\n", response.buffer);
        }
    }
    close(pipe_fd);

    pthread_create(&feed.receiver_thread, NULL, receive_messages, &feed);
    pthread_create(&feed.command_thread, NULL, capture_commands, &feed);

    pthread_join(feed.receiver_thread, NULL);
    pthread_join(feed.command_thread, NULL);

    return EXIT_SUCCESS;
}