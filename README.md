# SocialConnect API

> API RESTful de gestão para instituições sociais (ONGs, bancos de alimentos, CRAS, abrigos). Conecta doadores, voluntários, beneficiários e produtos em estoque.

**Disciplina:** Tópicos Especiais em Sistemas para Internet III  
**Stack:** Java 21 LTS · Spring Boot 4.1.1 · Spring Data JPA · Flyway · H2 (dev) · PostgreSQL (prod/testcontainers) · SpringDoc OpenAPI 3.1.0

---

## 🚀 Como Rodar

### Pré-requisitos
- **JDK 21 LTS** ([Adoptium](https://adoptium.net/))
- **Maven 3.9+** (ou utilize o wrapper `./mvnw` / `mvnw.cmd`)
- **Docker** (opcional, para execução de testes com Testcontainers)

### Execução local
```bash
# 1. Compilar o projeto
./mvnw clean compile

# 2. Rodar os testes automatizados
./mvnw test

# 3. Iniciar a aplicação
./mvnw spring-boot:run
```

A aplicação subirá em: `http://localhost:8080`

### 📖 Documentação e Swagger UI
- **Swagger UI Interativo:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Docs:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- **Console H2 Database:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:socialconnectdb`, Usuário: `sa`, Senha em branco)

---

## 📦 Módulo de Produtos

O módulo de produtos é responsável pelo controle rigoroso de estoque físico recebido pela ONG SocialConnect.

### Regras de Negócio
1. **Padrão de Chave Primária:** Chaves primárias com prefixo `id_` (`id_produto` na tabela `produtos`).
2. **Estoque Não Negativo:** Qualquer operação que resulte em `estoqueAtual < 0` é rejeitada, retornando HTTP `422 Unprocessable Entity` com Problem Details (RFC 7807).
3. **Alerta de Estoque Baixo:** O campo `estoqueBaixo` no DTO de resposta é calculado dinamicamente como `true` sempre que `estoqueAtual < estoqueMinimo`.
4. **Unicidade de Nome:** Não é permitido o cadastro ou alteração para um produto com nome já existente no sistema, retornando HTTP `409 Conflict`.
5. **Validação Customizada:** Implementação da anotação `@EstoqueNaoNegativo` com `EstoqueNaoNegativoValidator` para validação Bean Validation a nível de componente/DTO.
6. **I18n e Problem Details:** Mensagens internacionalizadas em `messages.properties` e erros padronizados no formato RFC 7807 (`ProblemDetail`).

### 📌 Endpoints do Módulo de Produtos

| Método | Endpoint | Descrição | Códigos de Retorno |
|---|---|---|---|
| `GET` | `/api/v1/produtos?page=0&size=10&sort=nome,asc` | Lista paginada com filtros opcionais (`nome`, `categoria`) | `200`, `400` |
| `GET` | `/api/v1/produtos/{id_produto}` | Busca produto detalhado por ID | `200`, `404` |
| `POST` | `/api/v1/produtos` | Cadastra novo produto (retorna cabeçalho `Location`) | `201`, `400`, `409`, `422` |
| `PUT` | `/api/v1/produtos/{id_produto}` | Atualização completa dos dados do produto | `200`, `400`, `404`, `409`, `422` |
| `DELETE` | `/api/v1/produtos/{id_produto}` | Remove um produto pelo ID | `204`, `404` |

#### Exemplo de Payload de Criação (POST /api/v1/produtos)
```json
{
  "nome": "Arroz 5kg",
  "categoria": "ALIMENTO",
  "estoqueAtual": 3,
  "estoqueMinimo": 10,
  "unidadeMedida": "unidade"
}
```

#### Exemplo de Resposta (ProdutoResponseDTO)
```json
{
  "idProduto": 1,
  "nome": "Arroz 5kg",
  "categoria": "ALIMENTO",
  "estoqueAtual": 3,
  "estoqueMinimo": 10,
  "unidadeMedida": "unidade",
  "dataCadastro": "2026-10-02",
  "estoqueBaixo": true
}
```

---

## 🧪 Testes Automatizados

O projeto conta com ampla cobertura de testes:
- **Testes Unitários:** `ProdutoServiceTest` utilizando JUnit 5 + Mockito com padrão AAA (*Arrange*, *Act*, *Assert*) cobrindo criação, unicidade, estoque negativo, estoque baixo, busca, atualização e exclusão.
- **Testes de Controller:** `ProdutoControllerTest` utilizando `@WebMvcTest` + MockMvc cobrindo a semântica HTTP de todos os endpoints e respostas de status (200, 201, 204, 400, 404, 409, 422).
- **Testes de Integração:** `ProdutoControllerIntegrationTest` utilizando `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Testcontainers` com PostgreSQL 17.