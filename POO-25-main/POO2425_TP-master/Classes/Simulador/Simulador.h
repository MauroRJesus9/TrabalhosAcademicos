//
// Created by mauro on 11/26/2024.
//

#ifndef SIMULADOR_H
#define SIMULADOR_H

#include <vector>
#include <memory>
#include <unordered_map>
#include "../Mapa/Mapa.h"
#include "../Buffer/Buffer.h"
#include "../Caravanas/Caravana.h"
#include "../Itens/Itens.h"

using namespace std;

class Simulador {
    unique_ptr<Buffer> buffer;
    unique_ptr<Mapa> mapa;
    vector<shared_ptr<Caravana>> caravanas;
    vector<std::shared_ptr<Itens>> itens;
    unordered_map<string, vector<string>> estadosBuffer;

    int turnoAtual;
    int moedas;
    int instantesEntreNovosItens;
    int duracaoItem;
    int maxItens;
    int precoVendaMercadoria;
    int precoCompraMercadoria;
    int precoCaravana;
    int instantesEntreNovosBarbaros;
    int duracaoBarbaros;

public:
    Simulador();
    // Processamento de comandos
    void processarComando(const string& comando);

    // Configuração inicial
    void comandoLerConfig(const string &nomeArquivo);

    // Eventos e interações no mapa
    void realizarCombate(const shared_ptr<Caravana> &caravana1, const shared_ptr<Caravana> &caravana2);
    void comandoTripul(int id, int quantidade);
    void comandoMoedas(int quantidade);
    void comandoBarbaro(int linha, int coluna);
    void comandoStopComportamento(int id);
    void comandoAutoComportamento(int caravana);
    shared_ptr<Caravana> encontrarCaravana(int id);
    void comandoMove(int id, const string &direcao, const int &distancia);
    void comandoVende(int id);
    void comandoCompra(int id, int toneladas);
    void comandoCaravana(int id);
    void comandoCidade(const char &cidade);
    void comandoPrecos();
    void comandoComprac(const char &cidade, const std::string &tipoCaravana);
    void comandoAreias(int l, int c, int r);
    void destruirCaravana(const shared_ptr<Caravana>& caravana);
    void gerarItem();
    void verificarItens();
    void aplicarEfeitoItem(shared_ptr<Caravana> caravana, shared_ptr<Itens> item);
    vector<shared_ptr<Caravana>> obterCaravanasAdjacentes(const shared_ptr<Caravana> &caravana) const;

    // Execução de turnos
    void comandoProx(int n);

    // Comandos de estado do buffer
    void comandoSave(const string &nome);
    void comandoLoad(const string& nome);
    void comandoList();
    void comandoDelete(const string& nome);
    void comandoTerminar();


    int limiteTurnosSemTripulacao(char tipoCaravana) const;
};

#endif // SIMULADOR_H
