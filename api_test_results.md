# Testes da API SocialConnect

## POST http://localhost:8080/api/v1/beneficiarios
**Request Payload:**
```json
{
  "endereco": "Rua das Flores, 123",
  "telefone": "(11) 99999-9999",
  "cpf": "123.456.789-00",
  "nome": "João da Silva",
  "dataCadastro": "2026-09-04",
  "situacaoVulnerabilidade": "Desempregado"
}
```
**Response Status:** 201
**Response Payload:**
```json
{
  "idBeneficiario": 1,
  "nome": "João da Silva",
  "cpf": "123.456.789-00",
  "telefone": "(11) 99999-9999",
  "endereco": "Rua das Flores, 123",
  "situacaoVulnerabilidade": "Desempregado",
  "dataCadastro": "2026-09-04"
}
```

## GET http://localhost:8080/api/v1/beneficiarios
**Response Status:** 200
**Response Payload:**
```json
[
  {
    "idBeneficiario": 1,
    "nome": "João da Silva",
    "cpf": "123.456.789-00",
    "telefone": "(11) 99999-9999",
    "endereco": "Rua das Flores, 123",
    "situacaoVulnerabilidade": "Desempregado",
    "dataCadastro": "2026-09-04"
  }
]
```

## GET http://localhost:8080/api/v1/beneficiarios/1
**Response Status:** 200
**Response Payload:**
```json
{
  "idBeneficiario": 1,
  "nome": "João da Silva",
  "cpf": "123.456.789-00",
  "telefone": "(11) 99999-9999",
  "endereco": "Rua das Flores, 123",
  "situacaoVulnerabilidade": "Desempregado",
  "dataCadastro": "2026-09-04"
}
```

## GET http://localhost:8080/api/v1/doacoes?tipo=ALIMENTO&dataInicio=2026-01-01
**Response Status:** 200
**Response Payload:**
```json
{
  "content": [],
  "empty": true,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 0,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "unpaged": false
  },
  "size": 10,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "totalElements": 0,
  "totalPages": 0
}
```

## DELETE http://localhost:8080/api/v1/beneficiarios/1
**Response Status:** 204
**Response Payload:**
```json
""
```
