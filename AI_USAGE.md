# Registro de Uso de IA Generativa

> **Política da disciplina:** O uso de IA generativa é permitido como
> assistente. O discente é **integralmente responsável** por testar, auditar e
> defender todo o código entregue, independentemente de como foi gerado.

## Instruções

Para cada aula ou entrega, registre abaixo:
- **Data**
- **Ferramenta** (ChatGPT, Copilot, Claude, etc.)
- **Prompt(s) utilizado(s)** (resumo ou cópia)
- **O que foi feito com a saída** (copiado integralmente, adaptado, usado como referência, descartado)

---

## Registro

| Data | Aula | Ferramenta | Prompt (resumo) | Uso da saída |
|------|------|------------|-----------------|--------------|
| 04/09/2026 | Aula 04 | Gemini | "Como configurar Flyway no Spring Boot com ddl-auto=validate e convenção de nomes de arquivos V1__...sql?" | Usei como base para estruturar as migrações V1, V2 e V3 e configurar o application.properties. |
| 25/09/2026 | Aula 05 | Claude | "Qual a forma recomendada de implementar PUT para substituição total e PATCH para parcial usando DTOs no Spring Boot?" | Adaptei a lógica nos métodos de atualização do BeneficiarioService. |
| 25/09/2026 | Aula 06 | Gemini | "Como estruturar um @RestControllerAdvice para padronizar erros da API no formato RFC 7807 (Problem Details)?" | Implementei a classe GlobalExceptionHandler e o record ProblemDetail. |
| 25/09/2026 | Aula 07 | Claude | "Exemplo de teste unitário com Mockito no padrão AAA (Arrange, Act, Assert) para camada de Service." | Estruturei os testes unitários do BeneficiarioServiceTest e DoacaoServiceTest. |
| 02/10/2026 | Avaliação 1 | Gemini | "Como criar uma anotação de validação customizada (@EstoqueNaoNegativo) no Spring Boot usando ConstraintValidator para barrar estoque negativo?" | Adaptei a lógica para validar tanto no cadastro quanto na alteração de produtos. |
| 02/10/2026 | Avaliação 1 | Claude | "Como configurar teste de integração no Spring Boot com Testcontainers e PostgreSQL subindo na porta aleatória?" | Utilizado como referência para implementar o ProdutoControllerIntegrationTest. |
| 02/10/2026 | Avaliação 1 | Gemini | "Como documentar respostas de erro RFC 7807 (404, 409, 422) nas anotações @ApiResponse do SpringDoc OpenAPI?" | Apliquei as anotações nos métodos do ProdutoController. |

---

_Declaração: Ao submeter este repositório, confirmo que todo o código foi
revisado, testado e compreendido por mim._