package com.dat.dateca.importacao.domain.ports;

import java.util.UUID;

public interface ImportJobDispatcher {

    void dispatch(UUID importJobId);
}
