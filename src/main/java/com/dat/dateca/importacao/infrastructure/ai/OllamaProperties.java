package com.dat.dateca.importacao.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuração do adapter local (Ollama). Inferência em CPU é bem mais lenta que a API do Claude,
 * então os timeouts padrão são generosos — um bloco de 5 páginas pode levar minutos.
 */
@ConfigurationProperties(prefix = "dateca.ai.ollama")
public class OllamaProperties {

    private String baseUrl = "http://localhost:11434";

    /** Modelo com suporte a tool calling; trocável sem alterar código. */
    private String model = "qwen2.5:7b-instruct-q4_K_M";

    /** Teto de tokens de saída por chamada (Ollama: num_predict). */
    private int maxTokens = 4096;

    private int maxTokensCeiling = 16384;

    private int maxRetries = 1;

    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * Leitura bem generosa: sem GPU, medimos ~2 tokens/s neste hardware, então um bloco denso
     * (muitas questões, schema aninhado) pode levar 15-30 min. Ajuste conforme a sua máquina — em
     * GPU dá para reduzir bastante.
     */
    private Duration readTimeout = Duration.ofMinutes(30);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getMaxTokensCeiling() {
        return maxTokensCeiling;
    }

    public void setMaxTokensCeiling(int maxTokensCeiling) {
        this.maxTokensCeiling = maxTokensCeiling;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
