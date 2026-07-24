package com.dat.dateca.importacao.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Retorno bruto de uma chamada de tool-use: o objeto no schema pedido e se a geração foi cortada
 * por limite de tokens.
 */
record ToolCallOutcome(JsonNode input, boolean truncated) {
}
