package com.dat.dateca.importacao.infrastructure.ai;

/**
 * System prompts e schemas de saída compartilhados por todas as implementações de
 * {@link com.dat.dateca.importacao.domain.ports.VisionExtractionPort}. Ficam aqui, e não dentro de
 * um adapter específico, para que trocar de provedor não mude o que é pedido ao modelo — sem isso
 * qualquer comparação de qualidade entre provedores estaria medindo prompt, não modelo.
 */
public final class ExtractionPrompts {

    public static final String COVER_TOOL = "extrair_capa_prova";
    public static final String QUESTIONS_TOOL = "extrair_questoes_prova";
    public static final String ANSWER_KEY_TOOL = "extrair_gabarito_prova";

    public static final String TOOL_DESCRIPTION = "Retorna os dados extraídos em formato estruturado.";

    public static final String COVER_SYSTEM_PROMPT = """
            Você recebe as primeiras páginas de uma prova (texto ou imagem da página escaneada).
            Extraia apenas os metadados da capa/cabeçalho: título da prova, instituição/banca
            responsável, ano, edição (ex.: "1º semestre", "caderno 1") e área/disciplina, caso a
            prova seja de uma única área. Não invente informações que não estejam no documento;
            deixe o campo vazio quando não encontrar.
            """;

    public static final String QUESTION_SYSTEM_PROMPT = """
            Você recebe um bloco de páginas consecutivas de uma prova de múltipla escolha (texto
            nativo ou imagem da página escaneada). Extraia CADA questão completa encontrada nesse
            bloco: número da questão, enunciado completo, alternativas (rótulo de uma letra e
            texto), e a resposta correta quando o gabarito aparecer junto à questão. Se a questão
            estiver marcada como anulada (ex.: carimbo "ANULADA"), marque annulled=true e deixe
            correctAnswer vazio. Se não tiver certeza da transcrição (ex.: fórmula complexa,
            imagem de difícil leitura), marque needsReview=true. Não invente alternativas nem
            texto que não esteja no documento. Ignore questões que apareçam apenas parcialmente
            no início ou no fim do bloco caso o enunciado esteja cortado sem alternativas visíveis.
            """;

    /**
     * O pipeline passa por aqui páginas que são apenas <em>candidatas</em> a gabarito (numa prova
     * escaneada não há texto nativo para procurar a palavra "GABARITO"), então o prompt precisa
     * autorizar explicitamente a resposta vazia. Sem isso o modelo tende a inventar pares para
     * satisfazer o schema.
     */
    public static final String ANSWER_KEY_SYSTEM_PROMPT = """
            Você recebe páginas de uma prova que PODEM conter o gabarito (tabela ou lista que
            associa o número da questão à letra da alternativa correta).
            Se encontrar um gabarito, extraia todos os pares (número, letra) presentes.
            Se as páginas NÃO contiverem um gabarito, devolva a lista "answers" vazia.
            Nunca deduza, calcule ou invente respostas a partir do enunciado das questões: extraia
            somente pares que estejam explicitamente escritos como gabarito no documento.
            """;

    public static final String COVER_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "title": {"type": "string"},
                "institution": {"type": "string"},
                "year": {"type": "integer"},
                "edition": {"type": "string"},
                "subjectArea": {"type": "string"}
              },
              "required": ["title"]
            }
            """;

    public static final String QUESTIONS_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "questions": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "number": {"type": "integer"},
                      "statement": {"type": "string"},
                      "sourcePageNumber": {"type": "integer"},
                      "annulled": {"type": "boolean"},
                      "needsReview": {"type": "boolean"},
                      "correctAnswer": {"type": "string"},
                      "alternatives": {
                        "type": "array",
                        "items": {
                          "type": "object",
                          "properties": {
                            "label": {"type": "string"},
                            "text": {"type": "string"}
                          },
                          "required": ["label", "text"]
                        }
                      }
                    },
                    "required": ["number", "statement", "alternatives", "sourcePageNumber"]
                  }
                }
              },
              "required": ["questions"]
            }
            """;

    public static final String ANSWER_KEY_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "answers": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "number": {"type": "integer"},
                      "answer": {"type": "string"}
                    },
                    "required": ["number", "answer"]
                  }
                }
              },
              "required": ["answers"]
            }
            """;

    private ExtractionPrompts() {
    }
}
