#include "Simulador.h"
#include <sstream>
#include <iostream>

#include "../Caravanas/Militar/CaravanaMilitar.h"
#include "../Caravanas/Secreta/CaravanaSecreta.h"
#include "../Caravanas/Barbara/CaravanaBarbara.h"
#include "../Caravanas/Comercio/CaravanaComercio.h"
#include <fstream>
#include <algorithm>
#include <ctime>

Simulador::Simulador()
        : buffer(make_unique<Buffer>(10, 20)),
          mapa(nullptr), moedas(0), turnoAtual(0),
          instantesEntreNovosItens(0), duracaoItem(0), maxItens(0),
          precoVendaMercadoria(0), precoCompraMercadoria(0), precoCaravana(0),
          instantesEntreNovosBarbaros(0), duracaoBarbaros(0) {
    std::srand(static_cast<unsigned int>(std::time(nullptr)));
}

void Simulador::comandoLerConfig(const string &nomeArquivo) {
    ifstream cfg(nomeArquivo);
    if (!cfg.is_open()) {
        (*buffer).escrever("Erro: Nao foi possível abrir o arquivo de configuracao.\n");
        return;
    }

    string linha;
    int linhas = 0, colunas = 0;
    vector<string> mapaConfig;

    while (getline(cfg, linha)) {
        stringstream ss(linha);
        string chave;
        ss >> chave;

        if (chave == "linhas") {
            ss >> linhas;
        } else if (chave == "colunas") {
            ss >> colunas;
        } else if (linha.find('.') != string::npos || linha.find('+') != string::npos) {
            mapaConfig.push_back(linha);
        } else if (chave == "moedas") {
            ss >> moedas;
        } else if (chave == "instantes_entre_novos_itens") {
            ss >> instantesEntreNovosItens;
        } else if (chave == "duracao_item") {
            ss >> duracaoItem;
        } else if (chave == "max_itens") {
            ss >> maxItens;
        } else if (chave == "preco_venda_mercadoria") {
            ss >> precoVendaMercadoria;
        } else if (chave == "preco_compra_mercadoria") {
            ss >> precoCompraMercadoria;
        } else if (chave == "preco_caravana") {
            ss >> precoCaravana;
        } else if (chave == "instantes_entre_novos_barbaros") {
            ss >> instantesEntreNovosBarbaros;
        } else if (chave == "duracao_barbaros") {
            ss >> duracaoBarbaros;
        }
    }

    cfg.close();

    mapa = make_unique<Mapa>(linhas, colunas);

    buffer = make_unique<Buffer>(linhas, colunas);

    int idCaravanaBarbara = 0;
    int idCaravanaMilitar = 0;
    int idCaravanaSecreta = 0;

    // Configura as zonas no mapa
    for (int i = 0; i < mapaConfig.size(); ++i) {
        for (int j = 0; j < mapaConfig[i].size(); ++j) {
            char tipo = mapaConfig[i][j];

            if (tipo == '.') {
                // ZONA VAZIA - Deserto
                mapa->definirZona(i, j, make_shared<Deserto>());
            } else if (tipo == '+') {
                // MONTANHA
                mapa->definirZona(i, j, make_shared<Montanha>());
            } else if (islower(tipo)) {
                mapa->definirZona(i, j, make_shared<Cidade>(tipo));
            } else if (isdigit(tipo)) {
                // **CARAVANA COMERCIAL (0-9)**
                int id = tipo - '0'; // Converte o caractere '0'-'9' para o valor numérico correspondente
                auto caravana = make_shared<CaravanaComercio>(id, i, j);
                caravanas.push_back(caravana);
                mapa->definirZona(i, j, caravana);
            } else if (tipo == '!') {
                // **CARAVANA BÁRBARA**
                auto caravana = make_shared<CaravanaBarbara>(idCaravanaBarbara++, i, j);
                caravanas.push_back(caravana);
                // **Mostra o '!' no mapa**
                mapa->definirZona(i, j, caravana);
            } else if (tipo == 'M') {
                // **CARAVANA MILITAR**
                auto caravana = make_shared<CaravanaMilitar>(idCaravanaMilitar++, i, j);
                caravanas.push_back(caravana);
                // **Mostra o 'M' no mapa**
                mapa->definirZona(i, j, caravana);
            } else if (tipo == '?') {
                // **CARAVANA SECRETA**
                auto caravana = make_shared<CaravanaSecreta>(idCaravanaSecreta++, i, j);
                caravanas.push_back(caravana);
                // **Mostra o '?' no mapa**
                mapa->definirZona(i, j, caravana);
            } else {
                (*buffer).escrever("Caractere desconhecido no mapa: ");
                (*buffer).escrever(tipo+" na posicao ("+to_string(i)+", "+to_string(j)+to_string(i)+")\n");
            }
        }
    }

    (*buffer).escrever("Configuracao carregada.\n");
    mapa->mostrar(*buffer);
}

void Simulador::comandoProx(int n) {
    if (!mapa) {
        (*buffer).escrever("Erro: Mapa nao configurado.\n");
        return;
    }

    for (int i = 0; i < n; i++) {
        buffer->limpar();
        turnoAtual++;

        (*buffer).escrever("Turno " + std::to_string(turnoAtual) + " iniciado.\n");

        // Gerar novos itens se necessário
        if (turnoAtual % instantesEntreNovosItens == 0) {
            gerarItem();
        }

        std::vector<std::pair<int, int>> combatesRealizados;

        // Executar combates entre caravanas adjacentes
        for (const auto &caravana1 : caravanas) {
            for (const auto &caravana2 : caravanas) {
                if (caravana1 != caravana2 &&
                    mapa->estaAdjacente(caravana1->getLinha(), caravana1->getColuna(), caravana2->getLinha(),
                                        caravana2->getColuna())) {
                    int id1 = caravana1->getID();
                    int id2 = caravana2->getID();
                    if (id1 > id2) std::swap(id1, id2);

                    bool jaRealizado = std::any_of(combatesRealizados.begin(), combatesRealizados.end(),
                                                   [&](const std::pair<int, int> &par) {
                                                       return par.first == id1 && par.second == id2;
                                                   });

                    if (!jaRealizado) {
                        realizarCombate(caravana1, caravana2);
                        combatesRealizados.emplace_back(id1, id2);
                    }
                }
            }
        }

        // Atualizar comportamento das caravanas
        for (auto it = caravanas.begin(); it != caravanas.end();) {
            auto caravana = *it;

            if (caravana->getTripulantes() <= 0) {
                // Atualizar turnos sem tripulação
                caravana->setTurnosSemTripulantes(caravana->getTurnosSemTripulantes() + 1);
                caravana->comportamentoSemTripulacao();

                // Verificar se excedeu o limite de turnos sem tripulação
                if (caravana->getTurnosSemTripulantes() >= limiteTurnosSemTripulacao(caravana->getTipo())) {
                    mapa->removerElemento(caravana->getLinha(), caravana->getColuna());
                    (*buffer).escrever("Caravana ID " + std::to_string(caravana->getID()) +
                                       " desapareceu após estar sem tripulação por muito tempo.\n");
                    it = caravanas.erase(it); // Remover caravana
                    continue;
                }
            } else if (caravana->getIsAutonomo()) {
                // Coletar itens e caravanas próximos
                std::vector<std::shared_ptr<Itens>> itensProximos;
                std::vector<std::shared_ptr<Caravana>> caravanasProximas;

                for (const auto &item : itens) {
                    if (mapa->estaAdjacente(caravana->getLinha(), caravana->getColuna(),
                                            item->getLinha(), item->getColuna())) {
                        itensProximos.push_back(item);
                    }
                }

                for (const auto &outraCaravana : caravanas) {
                    if (caravana != outraCaravana &&
                        mapa->estaAdjacente(caravana->getLinha(), caravana->getColuna(),
                                            outraCaravana->getLinha(), outraCaravana->getColuna())) {
                        caravanasProximas.push_back(outraCaravana);
                    }
                }

                // Executar comportamento autônomo
                int linhaAntiga = caravana->getLinha();
                int colunaAntiga = caravana->getColuna();
                caravana->comportamentoAutonomo(itensProximos, caravanasProximas);

                // Atualizar posição no mapa
                mapa->removerElemento(linhaAntiga, colunaAntiga);
                mapa->definirZona(caravana->getLinha(), caravana->getColuna(), caravana);

                (*buffer).escrever("Caravana ID " + std::to_string(caravana->getID()) +
                                   " executou comportamento autonomo.\n");
            }

            ++it; // Avançar iterador
        }

        // Verificar itens capturados ou expirados
        verificarItens();

        // Mostrar mapa atualizado
        mapa->mostrar(*buffer);

        // Finalizar simulação se não houver moedas e caravanas
        if (moedas < precoCaravana && caravanas.empty()) {
            comandoTerminar();
            return;
        }
    }
}

void Simulador::processarComando(const string &comando) {
    stringstream ss(comando);
    string cmd;
    ss >> cmd;

    if (cmd == "config") {
        string nomeArquivo;
        ss >> nomeArquivo;
        comandoLerConfig(nomeArquivo);
    } else if (cmd == "sair") {
        (*buffer).escrever("Encerrando o programa...\n");
        std::exit(0);
    } else if (cmd == "prox") {
        int n = 1;
        ss >> n;
        comandoProx(n);
    } else if (cmd == "exec") {
        string nomeFicheiro;
        ss >> nomeFicheiro;
        buffer->escrever("Comando: exec ");
        buffer->escrever(nomeFicheiro);
    } else if (cmd == "comprac") {
        // Comando da fase 2
        char cidade;
        string tipoCaravana;
        ss >> cidade >> tipoCaravana;
        comandoComprac(cidade, tipoCaravana);
    } else if (cmd == "precos") {
        comandoPrecos();
    } else if (cmd == "cidade") {
        // Comando da fase 2
        char cidade;
        ss >> cidade;
        comandoCidade(cidade);
    } else if (cmd == "caravana") {
        // Comando da fase 2
        int caravana;
        ss >> caravana;
        comandoCaravana(caravana);
    } else if (cmd == "compra") {
        // Comando da fase 2
        int caravana;
        int mercadoria;
        ss >> caravana >> mercadoria;
        comandoCompra(caravana, mercadoria);
    } else if (cmd == "vende") {
        // Comando da fase 2
        int caravana;
        ss >> caravana;
        comandoVende(caravana);
    } else if (cmd == "move") {
        // Comando da fase 2
        string direcao;
        int caravana, distancia;
        ss >> caravana >> direcao >> distancia;
        comandoMove(caravana, direcao, distancia);
    } else if (cmd == "auto") {
        // Comando da fase 2
        int caravana;
        ss >> caravana;
        comandoAutoComportamento(caravana);
    } else if (cmd == "stop") {
        // Comando da fase 2
        int caravana;
        ss >> caravana;
        comandoStopComportamento(caravana);
    } else if (cmd == "barbaro") {
        // Comando da fase 2
        int linha, coluna;
        ss >> linha >> coluna;
        comandoBarbaro(linha, coluna);
    } else if (cmd == "areia") {
        int linha, coluna, raio;
        ss >> linha >> coluna >> raio;
        comandoAreias(linha, coluna, raio);
    } else if (cmd == "moedas") {
        // Comando da fase 2
        int quantidade;
        ss >> quantidade;
        comandoMoedas(quantidade);
    } else if (cmd == "tripul") {
        // Comando da fase 2
        int caravana;
        int tripulantes;
        ss >> caravana >> tripulantes;
        comandoTripul(caravana, tripulantes);
    } else if (cmd == "saves") {
        // Comando da fase 2
        string nome;
        ss >> nome;

        comandoSave(nome);
    } else if (cmd == "loads") {
        // Comando da fase 2
        string nome;
        ss >> nome;

        comandoLoad(nome);
    } else if (cmd == "lists") {
        comandoList();
    } else if (cmd == "dels") {
        // Comando da fase 2
        string nome;
        ss >> nome;

        comandoDelete(nome);
    } else if (cmd == "terminar") {
        comandoTerminar();
    } else {
        // Comando desconhecido
        buffer->escrever("Comando desconhecido: ");
        buffer->escrever(cmd);
    }
}

std::shared_ptr<Caravana> Simulador::encontrarCaravana(int id) {
    for (const auto &caravana: caravanas) {
        if (caravana->getID() == id) {
            return caravana;
        }
    }
    return nullptr;
}

void Simulador::gerarItem() {
    if (itens.size() >= maxItens) return;

    int linha, coluna;
    TipoItem tipo;

    do {
        linha = rand() % mapa->getLinhas();
        coluna = rand() % mapa->getColunas();
    } while (!std::dynamic_pointer_cast<Deserto>(mapa->obterZona(linha, coluna)));

    tipo = static_cast<TipoItem>(rand() % 5);

    auto item = std::make_shared<Itens>(tipo, duracaoItem, linha, coluna);
    itens.push_back(item);
    mapa->definirZona(linha, coluna, item);

    (*buffer).escrever("Novo item (" + item->getDescricao() + ") apareceu em ("
                       + std::to_string(linha) + ", " + std::to_string(coluna) + ")\n");
}


void Simulador::verificarItens() {
    for (auto it = itens.begin(); it != itens.end();) {
        auto item = *it;
        item->reduzirTempo();

        if (item->expirou()) {
            mapa->removerElemento(item->getLinha(), item->getColuna());
            it = itens.erase(it);
            (*buffer).escrever("Item expirado e removido do mapa.\n");
        } else {
            bool itemCapturado = false;
            for (const auto &caravana: caravanas) {
                if (mapa->estaAdjacente(caravana->getLinha(), caravana->getColuna(), item->getLinha(),
                                        item->getColuna())) {
                    aplicarEfeitoItem(caravana, item);
                    mapa->removerElemento(item->getLinha(), item->getColuna());
                    it = itens.erase(it);
                    (*buffer).escrever("Item ");
                    (*buffer).escrever(item->getDescricao());
                    (*buffer).escrever(" apanhado pela comandoCaravana ");
                    (*buffer).escrever(caravana->getID());
                    (*buffer).escrever("\n");
                    itemCapturado = true;
                    break;
                }
            }
            if (!itemCapturado) ++it;
        }
    }
}

void Simulador::aplicarEfeitoItem(std::shared_ptr<Caravana> caravana, std::shared_ptr<Itens> item) {
    switch (item->getTipoItem()) {
        case TipoItem::CaixaPandora:
            caravana->perderTripulantes(caravana->getTripulantes() * 0.2);
            (*buffer).escrever("Caravana ");
            (*buffer).escrever(caravana->getID());
            (*buffer).escrever(" perdeu 20% dos tripulantes!\n");
            break;
        case TipoItem::ArcaTesouro:
            moedas += moedas * 0.1;
            (*buffer).escrever("Ganhou 10% de moedas!\n");
            break;
        case TipoItem::Jaula:
            caravana->adicionarTripulantes(10);
            (*buffer).escrever("Caravana ");
            (*buffer).escrever(caravana->getID());
            (*buffer).escrever(" ganhou tripulantes!\n");
            break;
        case TipoItem::Mina:
            destruirCaravana(caravana);
            (*buffer).escrever("Caravana ");
            (*buffer).escrever(caravana->getID());
            (*buffer).escrever(" foi destruída pela mina!\n");
            break;
        case TipoItem::Surpresa:
            caravana->aumentarCargaTemporaria(20);
            (*buffer).escrever("Caravana ");
            (*buffer).escrever(caravana->getID());
            (*buffer).escrever(" recebeu um bônus surpresa!\n");
            break;
    }
}

void Simulador::comandoAreias(int l, int c, int r) {
    int inicioLinha = std::max(0, l - r);
    int inicioColuna = std::max(0, c - r);
    int fimLinha = std::min(mapa->getLinhas() - 1, l + r);
    int fimColuna = std::min(mapa->getColunas() - 1, c + r);

    (*buffer).escrever("Tempestade de areia em (");
    (*buffer).escrever(l);
    (*buffer).escrever(", ");
    (*buffer).escrever(c);
    (*buffer).escrever(") com raio ");
    (*buffer).escrever(r);
    (*buffer).escrever("\n");

    // Verificar quais caravanas estao dentro do raio da tempestade
    for (const auto &caravana: caravanas) {
        int linha = caravana->getLinha();
        int coluna = caravana->getColuna();

        // Verifica se a comandoCaravana está dentro da área da tempestade
        if (linha >= inicioLinha && linha <= fimLinha && coluna >= inicioColuna && coluna <= fimColuna) {
            (*buffer).escrever("Caravana ");
            (*buffer).escrever(caravana->getID());
            (*buffer).escrever(" afetada na posicao (");
            (*buffer).escrever(linha);
            (*buffer).escrever(", ");
            (*buffer).escrever(coluna);
            (*buffer).escrever(")\n");

            // Aplica o efeito da tempestade
            if (caravana->afetarPorTempestade()) {
                destruirCaravana(caravana);
            }
        }
    }

    // Mostrar mapa atualizado
    mapa->mostrar(*buffer);
}

void Simulador::destruirCaravana(const std::shared_ptr<Caravana> &caravana) {
    mapa->removerElemento(caravana->getLinha(), caravana->getColuna());
    caravanas.erase(
            std::remove(caravanas.begin(), caravanas.end(), caravana),
            caravanas.end()
    );
    (*buffer).escrever("Caravana ");
    (*buffer).escrever(caravana->getID());
    (*buffer).escrever(" foi destruída e removida do mapa.\n");
}

void Simulador::comandoSave(const string &nome) {
    vector<string> estadoAtual;

    // Captura o estado atual do buffer
    for (int i = 0; i < buffer->getLinhas(); ++i) {
        string linha;
        for (int j = 0; j < buffer->getColunas(); ++j) {
            linha.push_back(buffer->getChar(i, j));
        }
        estadoAtual.push_back(linha);
    }

    // Salva o estado
    estadosBuffer[nome] = estadoAtual;
    (*buffer).escrever("Estado do buffer salvo como: " + nome + "\n");
}

void Simulador::comandoLoad(const string &nome) {
    if (estadosBuffer.find(nome) == estadosBuffer.end()) {
        (*buffer).escrever("Erro: Estado " + nome + " nao encontrado.\n");
        return;
    }

    const auto &estado = estadosBuffer[nome];
    buffer->limpar();

    // Carrega o estado no buffer
    for (int i = 0; i < estado.size(); ++i) {
        for (int j = 0; j < estado[i].size(); ++j) {
            buffer->moverCursor(i, j);
            buffer->escrever(estado[i][j]);
        }
    }

    (*buffer).escrever("Estado " + nome + " carregado.\n");
    buffer->mostrar();
}

void Simulador::comandoList() {
    (*buffer).escrever("Estados salvos:\n");
    for (const auto &[nome, _]: estadosBuffer) {
        (*buffer).escrever(" - " + nome + "\n");
    }
}

void Simulador::comandoDelete(const string &nome) {
    if (estadosBuffer.erase(nome)) {
        (*buffer).escrever("Estado " + nome + " removido com sucesso.\n");
    } else {
        (*buffer).escrever("Erro: Estado " + nome + " nao encontrado.\n");
    }
}

void Simulador::comandoTerminar() {
    (*buffer).escrever(
            "Simulacao terminada. Pontuacao final: " + std::to_string(moedas) +
            ", Turnos percorridos: " + std::to_string(turnoAtual) + ".\n"
    );

    // Limpar todas as estruturas em memória
    mapa.reset();
    buffer.reset();
    caravanas.clear();
    itens.clear();
    estadosBuffer.clear();

    // Reiniciar o simulador para a fase inicial
    moedas = 0;
    turnoAtual = 0;
    instantesEntreNovosItens = 0;
    duracaoItem = 0;
    maxItens = 0;
    precoVendaMercadoria = 0;
    precoCompraMercadoria = 0;
    precoCaravana = 0;
    instantesEntreNovosBarbaros = 0;
    duracaoBarbaros = 0;

    // Reiniciar buffer e mapa
    buffer = std::make_unique<Buffer>(10, 20);
    (*buffer).escrever("Simulador pronto para nova configuracao.\n");
}

void Simulador::realizarCombate(const std::shared_ptr<Caravana> &caravana1,
                                const std::shared_ptr<Caravana> &caravana2) {
    // Verificar se sao inimigos
    if (!caravana1->verificarInimigo(caravana2)) {
        (*buffer).escrever("Caravanas nao sao inimigas. Combate ignorado.\n");
        return;
    }

    // Sorteio dos valores de combate
    int valor1 = rand() % (caravana1->getTripulantes() + 1);
    int valor2 = rand() % (caravana2->getTripulantes() + 1);

    (*buffer).escrever("Combate entre Caravana ");
    (*buffer).escrever(to_string(caravana1->getID()));
    (*buffer).escrever(" (Forca: ");
    (*buffer).escrever(to_string(valor1));
    (*buffer).escrever(") e Caravana ");
    (*buffer).escrever(to_string(caravana2->getID()));
    (*buffer).escrever(" (Forca: ");
    (*buffer).escrever(to_string(valor2));
    (*buffer).escrever(").\n");

    if (valor1 == valor2) {
        (*buffer).escrever("Empate no combate. Nenhuma comandoCaravana sofre perdas.\n");
        return;
    }

    // Determinar vencedora e perdedora
    std::shared_ptr<Caravana> vencedora = (valor1 > valor2) ? caravana1 : caravana2;
    std::shared_ptr<Caravana> perdedora = (valor1 > valor2) ? caravana2 : caravana1;
    int perdasVencedor = vencedora->getTripulantes() * 0.2;
    int perdasPerdedor = 2 * perdasVencedor;

    // Ajustar tripulantes
    vencedora->perderTripulantes(perdasVencedor);
    perdedora->perderTripulantes(perdasPerdedor);

    (*buffer).escrever("Caravana ");
    (*buffer).escrever(to_string(vencedora->getID()));
    (*buffer).escrever(" venceu o combate, perdendo ");
    (*buffer).escrever(to_string(perdasVencedor));
    (*buffer).escrever(" tripulantes.\n");

    (*buffer).escrever("Caravana ");
    (*buffer).escrever(to_string(perdedora->getID()));
    (*buffer).escrever(" perdeu o combate, perdendo ");
    (*buffer).escrever(to_string(perdasPerdedor));
    (*buffer).escrever(" tripulantes.\n");

    // Verificar destruicao da comandoCaravana perdedora
    if (perdedora->estaSemTripulantes()) {
        (*buffer).escrever("Caravana ");
        (*buffer).escrever(to_string(perdedora->getID()));
        (*buffer).escrever(" foi destruída em combate.\n");

        // Transferir água da comandoCaravana perdedora para a vencedora
        int aguaTransferida = std::min(perdedora->getAguaAtual(),
                                       vencedora->getCapacidadeAgua() - vencedora->getAguaAtual());
        vencedora->reabastecerAgua();
        vencedora->aumentarCargaTemporaria(aguaTransferida);

        destruirCaravana(perdedora);
    }
}

std::vector<std::shared_ptr<Caravana> > Simulador::obterCaravanasAdjacentes(
        const std::shared_ptr<Caravana> &caravana) const {
    std::vector<std::shared_ptr<Caravana> > adjacentes;

    for (const auto &outraCaravana: caravanas) {
        if (caravana == outraCaravana) continue;

        if (mapa->estaAdjacente(caravana->getLinha(), caravana->getColuna(), outraCaravana->getLinha(),
                                outraCaravana->getColuna())) {
            if (caravana->verificarInimigo(outraCaravana)) {
                adjacentes.push_back(outraCaravana);
            }
        }
    }

    return adjacentes;
}

void Simulador::comandoComprac(const char &cidade, const std::string &tipoCaravana) {
    int linhaCidade = -1;
    int colunaCidade = -1;
    bool cidadeEncontrada = false;

    // Iterar pelo mapa para localizar a cidade
    for (int i = 0; i < mapa->getLinhas(); ++i) {
        for (int j = 0; j < mapa->getColunas(); ++j) {
            auto zona = mapa->obterZona(i, j);
            if (zona && zona->getTipo() == cidade) {
                linhaCidade = i;
                colunaCidade = j;
                cidadeEncontrada = true;
                break;
            }
        }
        if (cidadeEncontrada) break;
    }

    if (!cidadeEncontrada) {
        (*buffer).escrever("Erro: Cidade " + std::string(1, cidade) + " nao encontrada.\n");
        return;
    }

    if (moedas < precoCaravana) {
        (*buffer).escrever("Erro: Moedas insuficientes para comprar caravana.\n");
        return;
    }

    std::shared_ptr<Caravana> novaCaravana;

    if (tipoCaravana == "C") {
        novaCaravana = std::make_shared<CaravanaComercio>(caravanas.size(), linhaCidade, colunaCidade);
    } else if (tipoCaravana == "M") {
        novaCaravana = std::make_shared<CaravanaMilitar>(caravanas.size(), linhaCidade, colunaCidade);
    } else if (tipoCaravana == "S") {
        novaCaravana = std::make_shared<CaravanaSecreta>(caravanas.size(), linhaCidade, colunaCidade);
    } else {
        (*buffer).escrever("Erro: Tipo de caravana inválido.\n");
        return;
    }

    caravanas.push_back(novaCaravana);
    mapa->definirZona(linhaCidade, colunaCidade, novaCaravana);
    moedas -= precoCaravana;

    (*buffer).escrever("Caravana do tipo " + tipoCaravana + " comprada na cidade " + std::string(1, cidade) + ".\n");
}

void Simulador::comandoPrecos() {
    (*buffer).escrever("Precos:\n");
    (*buffer).escrever("Venda de mercadorias: " + std::to_string(precoVendaMercadoria) + " moedas por tonelada.\n");
    (*buffer).escrever("Compra de mercadorias: " + std::to_string(precoCompraMercadoria) + " moedas por tonelada.\n");
    (*buffer).escrever("Preco de comandoCaravana: " + std::to_string(precoCaravana) + " moedas.\n");
}

void Simulador::comandoCidade(const char &cidade) {
    int linhaCidade = -1;
    int colunaCidade = -1;
    bool cidadeEncontrada = false;

    // Iterar pelo mapa para localizar a cidade
    for (int i = 0; i < mapa->getLinhas(); ++i) {
        for (int j = 0; j < mapa->getColunas(); ++j) {
            auto zona = mapa->obterZona(i, j);
            if (zona && zona->getTipo() == cidade) {
                linhaCidade = i;
                colunaCidade = j;
                cidadeEncontrada = true;
                break;
            }
        }
        if (cidadeEncontrada) break;
    }

    if (!cidadeEncontrada) {
        (*buffer).escrever("Erro: Cidade " + std::string(1, cidade) + " nao encontrada.\n");
        return;
    }

    (*buffer).escrever("Conteúdo da cidade " + std::string(1, cidade) + ":\n");

    // Iterar pelas caravanas para verificar quais estao na posicao da cidade
    for (const auto &caravana: caravanas) {
        if (caravana->getLinha() == linhaCidade && caravana->getColuna() == colunaCidade) {
            (*buffer).escrever(" - Caravana ID: " + std::to_string(caravana->getID()) + "\n");
        }
    }
}

void Simulador::comandoCaravana(int id) {
    auto it = std::find_if(caravanas.begin(), caravanas.end(),
                           [id](const std::shared_ptr<Caravana> &c) { return c->getID() == id; });

    if (it == caravanas.end()) {
        (*buffer).escrever("Erro: Caravana com ID " + std::to_string(id) + " nao encontrada.\n");
        return;
    }

    const auto &caravana = *it;
    (*buffer).escrever("Detalhes da comandoCaravana ID: " + std::to_string(id) + "\n");
    (*buffer).escrever(" - Tipo: " + std::string(1, caravana->getTipo()) + "\n");
    (*buffer).escrever(" - Tripulantes: " + std::to_string(caravana->getTripulantes()) + "\n");
    (*buffer).escrever(" - Carga Atual: " + std::to_string(caravana->getCargaAtual()) + " toneladas\n");
    (*buffer).escrever(" - Água Atual: " + std::to_string(caravana->getAguaAtual()) + " litros\n");
}

void Simulador::comandoCompra(int id, int toneladas) {
    auto caravana = encontrarCaravana(id);
    if (!caravana) {
        (*buffer).escrever("Erro: Caravana com ID " + std::to_string(id) + " nao encontrada.\n");
        return;
    }

    if (moedas < toneladas * precoCompraMercadoria) {
        (*buffer).escrever("Erro: Moedas insuficientes para comprar mercadorias.\n");
        return;
    }

    if (caravana->getCargaAtual() + toneladas > caravana->getCapacidadeCarga()) {
        (*buffer).escrever("Erro: Carga excede a capacidade da comandoCaravana.\n");
        return;
    }

    caravana->aumentarCargaTemporaria(toneladas);
    moedas -= toneladas * precoCompraMercadoria;
    (*buffer).escrever(
            "Caravana ID " + std::to_string(id) + " comprou " + std::to_string(toneladas) + " toneladas de mercadorias.\n");
}

void Simulador::comandoVende(int id) {
    auto caravana = encontrarCaravana(id);
    if (!caravana) {
        (*buffer).escrever("Erro: Caravana com ID " + std::to_string(id) + " nao encontrada.\n");
        return;
    }

    int cargaAtual = caravana->getCargaAtual();
    moedas += cargaAtual * precoVendaMercadoria;
    caravana->aumentarCargaTemporaria(-cargaAtual);

    (*buffer).escrever(
            "Caravana ID " + std::to_string(id) + " vendeu " + std::to_string(cargaAtual) + " toneladas de mercadorias.\n");
}

void Simulador::comandoMove(int id, const string& direcao, const int& distancia) {
    auto caravana = encontrarCaravana(id);
    if (!caravana) {
        (*buffer).escrever("Erro: Caravana com ID " + std::to_string(id) + " não encontrada.\n");
        return;
    }

    int colunaOld = caravana->getColuna();
    int linhaOld = caravana->getLinha();

    if (caravana->mover(direcao, distancia, mapa->getLinhas(), mapa->getColunas())) {
        char tipo = mapa->obterZona(linhaOld,colunaOld)->getTipo();
        if (tipo == '.' || isdigit(tipo)) {
            mapa->removerElemento(linhaOld, colunaOld); // Remove da posição antiga
        }
        if (mapa->obterZona(caravana->getLinha(), caravana->getColuna())->getTipo() == '.')
            mapa->definirZona(caravana->getLinha(), caravana->getColuna(), caravana); // Adiciona na nova posição

        (*buffer).escrever("Caravana ID " + std::to_string(id) + " movida na direcao " + direcao + ".\n");

        mapa->mostrar(*buffer);
    } else {
        (*buffer).escrever("Caravana ID " + std::to_string(id) + " nao foi movida devido a um erro.\n");
    }
}

void Simulador::comandoAutoComportamento(int id) {
    auto caravana = encontrarCaravana(id);
    if (!caravana) {
        (*buffer).escrever("Erro: Caravana com ID " + std::to_string(id) + " nao encontrada.\n");
        return;
    }
    caravana->setIsAutonomo(!caravana->getIsAutonomo());
    (*buffer).escrever("Caravana ID " + std::to_string(id) + " agora está em modo automático.\n");
}

void Simulador::comandoStopComportamento(int id) {
    auto caravana = encontrarCaravana(id);
    if (!caravana) {
        (*buffer).escrever("Erro: Caravana com ID " + std::to_string(id) + " nao encontrada.\n");
        return;
    }

    caravana->setIsAutonomo(false);
}

void Simulador::comandoBarbaro(int linha, int coluna) {
    if (!mapa->zonaValida(linha, coluna)) {
        (*buffer).escrever("Erro: Posicao inválida no mapa.\n");
        return;
    }

    auto novaBarbara = std::make_shared<CaravanaBarbara>(caravanas.size(), linha, coluna);
    caravanas.push_back(novaBarbara);
    mapa->definirZona(linha, coluna, novaBarbara);

    (*buffer).escrever(
            "Caravana bárbara criada na posicao (" + std::to_string(linha) + ", " + std::to_string(coluna) + ").\n");
}

void Simulador::comandoMoedas(int quantidade) {
    moedas += quantidade;
    (*buffer).escrever("Número de moedas ajustado para " + std::to_string(moedas) + ".\n");
}

void Simulador::comandoTripul(int id, int quantidade) {
    auto caravana = encontrarCaravana(id);
    if (!caravana) {
        (*buffer).escrever("Erro: Caravana com ID " + std::to_string(id) + " nao encontrada.\n");
        return;
    }

    if (moedas < quantidade) {
        (*buffer).escrever("Erro: Moedas insuficientes para comprar tripulantes.\n");
        return;
    }

    caravana->adicionarTripulantesComando(quantidade);
    moedas -= quantidade;

    (*buffer).escrever(
            "Adicionados " + std::to_string(quantidade) + " tripulantes à comandoCaravana ID " + std::to_string(id) +
            ".\n");
}

int Simulador::limiteTurnosSemTripulacao(char tipoCaravana) const {
    switch (tipoCaravana) {
        case 'M': return 7; // Militar
        case 'C': return 5; // Comércio
        case 'B': return 0; // Bárbara (desaparece imediatamente)
        case 'S': return 6; // Secreta
        default: return 5;  // Padrão
    }
}