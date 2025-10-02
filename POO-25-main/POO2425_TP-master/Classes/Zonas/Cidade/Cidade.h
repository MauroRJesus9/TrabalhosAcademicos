//
// Created by mauro on 11/26/2024.
//

#ifndef CIDADE_H
#define CIDADE_H
#include "../Deserto.h"


class Cidade : public Deserto {
    char nome; // Nome da comandoCidade (uma letra)
public:
    explicit Cidade(char n) : nome(n) {}
    char getTipo() const override { return nome; }
};

#endif // CIDADE_H
