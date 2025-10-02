//
// Created by mauro on 11/26/2024.
//

#ifndef MONTANHA_H
#define MONTANHA_H
#include "../Deserto.h"

class Montanha : public Deserto {
public:
    char getTipo() const override { return '+'; }
};

#endif // MONTANHA_H
