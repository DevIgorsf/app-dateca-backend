package com.dat.dateca.importacao.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "dateca.ai.anthropic")
public class AnthropicProperties {

    private String apiKey;
    private String model = "claude-sonnet-5";
    private String baseUrl = "https://api.anthropic.com";
    private String version = "2023-06-01";
    private int maxRetries = 2;

    /** Teto inicial de tokens de saída por chamada. */
    private int maxTokens = 4096;

    /**
     * Teto máximo ao qual {@link #maxTokens} pode ser escalado quando a resposta vem truncada.
     * Um bloco de 5 páginas com enunciados longos estoura 4096 com facilidade.
     */
    private int maxTokensCeiling = 16384;

    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofMinutes(3);

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
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
