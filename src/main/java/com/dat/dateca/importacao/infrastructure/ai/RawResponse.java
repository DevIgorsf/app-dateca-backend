package com.dat.dateca.importacao.infrastructure.ai;

import org.springframework.http.HttpStatusCode;

/**
 * Resposta HTTP crua (status + corpo em bytes), lida via {@code RestClient.exchange} sem passar por
 * um conversor de leitura. Compartilhada pelos adapters de IA.
 */
record RawResponse(HttpStatusCode status, byte[] body) {
}
