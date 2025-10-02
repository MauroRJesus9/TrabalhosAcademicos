#ifndef UTILIDADES_H
#define UTILIDADES_H

#include <stdbool.h>
#include <stdlib.h>
#include <pthread.h> 
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <signal.h>
#include <sys/stat.h>
#include <ctype.h>

// Constantes
#define MAX_USERS 10            
#define MAX_TOPICOS 20        
#define MAX_MENSAGENS_POR_TOPICO 5    
#define TAMANHO_MAX_MENSAGEM 300  
#define TAMANHO_NOME_TOPICO 20        
#define TAMANHO_NOME_USER 20 

#define PIPE_MANAGER_TO_CLIENT "./manager_to_"
#define PIPE_CLIENT_TO_MANAGER "./client_to_manager"


// Estruturas
typedef struct {
    char username[TAMANHO_NOME_USER];
    char buffer[TAMANHO_MAX_MENSAGEM];
    int tempo;
    char tipo[20];
    char topico[TAMANHO_NOME_TOPICO];
} Mensagem;

typedef struct {
    char nome[TAMANHO_NOME_TOPICO];
    Mensagem mensagens[MAX_MENSAGENS_POR_TOPICO]; 
    int msg_count;
    bool trinco;
} Topico;

typedef struct {
    char username[50];
    char topicos_subscritos[MAX_TOPICOS][TAMANHO_NOME_TOPICO];
    int n_topicos;
} User;

typedef struct {
    Topico topicos[MAX_TOPICOS];
    User users[MAX_USERS];
    int n_users;
    int n_topicos;      
    bool server_running;
    pthread_mutex_t trinco; 
    pthread_t timer_thread;
    pthread_t admin_thread;
} Manager;

typedef struct {
    char client_fifo[100];
    Mensagem msg; 
    Manager *manager;      
} ThreadData;

typedef struct {
    char username[50];         
    pthread_t receiver_thread; 
    pthread_t command_thread;
    char client_fifo[100];
} Feed;

// Funções
int cria_fifo(const char *path);
void write_to_manager(const Mensagem* msg);

//Interface
void divisoria();
void divisoriaCliente();

#endif
