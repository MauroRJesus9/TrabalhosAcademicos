//
// Created by mauro on 11/26/2024.
//

#ifndef DESERTO_H
#define DESERTO_H

#include <memory>
#include "Zona.h"

class Deserto: public Zona {
public:
    virtual char getTipo() const { return '.'; }
    virtual ~Deserto() = default;
};

#endif // DESERTO_H
