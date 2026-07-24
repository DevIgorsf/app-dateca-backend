package com.dat.dateca.importacao.infrastructure.ai;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

/**
 * Fábrica de {@link ClientHttpRequestFactory} com timeouts explícitos. A inferência local em CPU é
 * lenta, então cada adapter configura os seus próprios (o do Ollama é bem mais generoso que o da
 * Anthropic) em vez de herdar o default do {@code RestClient}.
 */
final class RestClientFactories {

    static ClientHttpRequestFactory withTimeouts(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) connectTimeout.toMillis());
        factory.setReadTimeout((int) readTimeout.toMillis());
        return factory;
    }

    private RestClientFactories() {
    }
}
