//
// Created by mauro on 11/26/2024.
//

#ifndef CARAVANA_H
#define CARAVANA_H

#include <string>
#include <vector>
#include "../Zonas/Deserto.h"
#include "../Itens/Itens.h"

using namespace std;

class Caravana : public Zona {
protected:
    int id;
    char tipo; // Tipo ('C' = Comercial, 'M' = Militar, 'S' = Secreta, 'B' = Bárbara)
    int linha, coluna; // Posição no mapa
    int tripulantes;
    int capacidadeCarga;
    int cargaAtual;
    int capacidadeAgua;
    int aguaAtual;
    int deslocacaoPturno, gastAgua;
    bool isAutonomo;
    int turnosSemTripulantes = 0;
public:
    int getTurnosSemTripulantes() const;
    void setTurnosSemTripulantes(int turnosSemTripulantes);

public:
    Caravana(int id, int linha, int coluna);
    virtual void comportamentoAutonomo(
    const std::vector<std::shared_ptr<Itens>> &itensProximos,
    const std::vector<std::shared_ptr<Caravana>> &caravanasProximas) = 0;
    virtual void comportamentoSemTripulacao() = 0;
    virtual bool afetarPorTempestade() = 0;
    virtual char getTipo() const = 0;
    virtual bool verificarInimigo(const shared_ptr<Caravana> &outraCaravana) = 0;
    virtual void beberAgua() = 0;
    virtual ~Caravana() = default;

    int getID() const { return id; }
    int getLinha() const { return linha; }
    int getColuna() const { return coluna; }
    int getTripulantes() const { return tripulantes; }
    int getCargaAtual() const { return cargaAtual; }
    int getAguaAtual() const;
    int getDeslocacaoPturno() const;
    int getGastAgua() const;
    int getCapacidadeAgua() const;
    int getCapacidadeCarga() const;

    void setIsAutonomo(bool isAutonomo) { this->isAutonomo = isAutonomo; }
    bool getIsAutonomo() const { return isAutonomo; };
    void perderTripulantes(int quantidade);
    void adicionarTripulantes(int quantidade);
    void aumentarCargaTemporaria(int quantidade);
    bool estaSemTripulantes() const { return tripulantes <= 0; }
    void reabastecerAgua() { aguaAtual = capacidadeAgua; }
    void adicionarTripulantesComando(int quantidade);
    bool mover(const string &direcao, const int &dist, int maxLinhas, int maxColunas);
    void comportamentoGeralAutonomo();
};

#endif // CARAVANA_H
