package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seleciona a implementação de {@link VisionExtractionPort} a partir de
 * {@code vision-extraction.provider} (default: {@code anthropic}).
 *
 * <ul>
 *   <li>{@code anthropic} — tudo (texto e imagem) vai para a Anthropic. Comportamento anterior.</li>
 *   <li>{@code ollama} — roteamento híbrido: blocos de texto nativo no Ollama local; blocos que
 *   contêm páginas rasterizadas (provas escaneadas) continuam na Anthropic, por serem mais
 *   exigentes em qualidade de visão. Requer o Ollama no ar; requer a chave da Anthropic apenas se
 *   aparecerem páginas escaneadas.</li>
 * </ul>
 *
 * <p>As dependências entram como parâmetros dos métodos {@code @Bean} (não no construtor da classe)
 * para não forçar a criação do {@code ObjectMapper}/properties na fase de processamento das
 * configurações, antes de estarem prontos. O {@code ObjectMapper} é próprio (Jackson 2): os
 * adapters só fazem leitura/escrita genérica de árvore JSON, então não dependemos do mapper do
 * contexto — que no Spring Boot 4 passou a ser Jackson 3.
 */
@Configuration
@EnableConfigurationProperties({AnthropicProperties.class, OllamaProperties.class})
public class VisionExtractionConfig {

    private static final Logger log = LoggerFactory.getLogger(VisionExtractionConfig.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public AnthropicVisionExtractionAdapter anthropicVisionExtractionAdapter(AnthropicProperties anthropicProperties) {
        return new AnthropicVisionExtractionAdapter(anthropicProperties, objectMapper);
    }

    @Bean
    public VisionExtractionPort visionExtractionPort(
            AnthropicVisionExtractionAdapter anthropicAdapter,
            OllamaProperties ollamaProperties,
            AnthropicProperties anthropicProperties,
            @Value("${vision-extraction.provider:anthropic}") String provider) {

        String normalized = provider == null ? "anthropic" : provider.trim().toLowerCase();
        return switch (normalized) {
            case "ollama" -> {
                log.info("Provedor de extração por visão: OLLAMA (texto local em {} usando o modelo '{}'; "
                                + "páginas escaneadas continuam na Anthropic)",
                        ollamaProperties.getBaseUrl(), ollamaProperties.getModel());
                VisionExtractionPort ollamaAdapter =
                        new OllamaVisionExtractionAdapter(ollamaProperties, objectMapper);
                yield new RoutingVisionExtractionAdapter(ollamaAdapter, anthropicAdapter);
            }
            case "anthropic" -> {
                log.info("Provedor de extração por visão: ANTHROPIC (modelo '{}')", anthropicProperties.getModel());
                yield anthropicAdapter;
            }
            default -> throw new IllegalStateException("vision-extraction.provider inválido: '" + provider
                    + "'. Valores aceitos: anthropic, ollama.");
        };
    }
}
