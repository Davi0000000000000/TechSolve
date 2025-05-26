# TechSolve API - Sistema de Chamados Técnicos

## 🧭 Objetivo e Público-Alvo

Esta API tem como objetivo permitir o gerenciamento de chamados técnicos em uma organização. Foi projetada para ser utilizada por três tipos de usuários: **Usuários comuns**, **Técnicos** e **Administradores**, cada um com permissões específicas de acesso e operação sobre os chamados.

O sistema pode ser utilizado por empresas, equipes de suporte técnico ou instituições que necessitem registrar, acompanhar e resolver chamados de forma estruturada e segura.

---

## ✅ Funcionalidades Implementadas

As funcionalidades foram baseadas em histórias de usuário com papéis bem definidos. Algumas das principais incluem:

- 🔐 Autenticação e autorização com JWT
- 👤 Cadastro e login de usuários
- 🎫 Abertura de chamado técnico por usuários
- 🧑‍🔧 Visualização e aceitação de chamados por técnicos
- ✅ Finalização de chamados com registro de data de resolução
- 🗂️ Visualização de chamados por status e prioridade
- 🔎 Filtros por setor, urgência, data de abertura e resolução
- 🧪 Testes unitários e funcionais com validações de fluxo completo
- 📊 Dashboard com estatísticas de tempo médio de resposta por técnico
---

## ⚙️ Instruções de Execução Local

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Banco de dados PostgreSQL ou H2 (configurável)
- IDE como IntelliJ ou Eclipse

### Build e Run

```bash
# Clonar o repositório
git clone https://github.com/Davi0000000000000/TechSolve/blob/main/TechSolve.rar
cd techsolve-api

# Compilar e empacotar o projeto
mvn clean install

# Rodar o projeto localmente
mvn spring-boot:run

```

## 🔐 Como Obter o Token JWT e Testar Endpoints

1. **Cadastrar um novo usuário (visitante):**

POST /api/auth/register
Content-Type: application/json

{
    "name": "João da Silva",
    "email": "joao@email.com",
    "password": "123456",
    "confirmPassword": "123456"
}


2. **Realizar login para obter o token JWT:**

POST /api/auth/login
Content-Type: application/json

{
    "email": "joao@email.com",
    "password": "123456"
}


3. **Autenticar-se com o token:**

Após o login, copie o token JWT retornado e use-o no header das requisições protegidas:


4. **Testar via Swagger (interface gráfica):**

Acesse: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧱 Resumo do Modelo de Dados e Regras de Validação

### 🧑‍💼 User

| Campo     | Tipo     | Validação                            |
|-----------|----------|--------------------------------------|
| name      | String   | Obrigatório, mínimo 3 caracteres     |
| email     | String   | Obrigatório, formato válido e único |
| password  | String   | Obrigatório, mínimo 6 caracteres     |
| role      | Enum     | USER, TECHNICIAN, ADMIN              |

### 🎫 Ticket

| Campo            | Tipo      | Validação                                        |
|------------------|----------|-------------------------------------------------|
| title            | String   | Obrigatório                                     |
| description      | String   | Obrigatório                                     |
| priority         | Enum     | BAIXA, MEDIA, ALTA                              |
| department       | Enum     | SUPORTE, TI, FINANCEIRO, RH                     |
| status           | Enum     | ABERTO, EM_ATENDIMENTO, CONCLUIDO               |
| createdAt        | DateTime | Gerado automaticamente                          |
| resolvedAt       | DateTime | Gerado automaticamente ao resolver              |
| technician       | User     | Definido ao aceitar chamado (role: TECHNICIAN)  |
| user             | User     | Associado automaticamente (quem criou)          |

---

## 🔒 Descrição do Funcionamento da Autenticação e Autorização

A autenticação da API é feita via **JWT (JSON Web Token)**:

- O usuário faz login com e-mail e senha.
- Um token JWT é gerado e enviado na resposta.
- Esse token deve ser enviado no cabeçalho `Authorization` em cada requisição autenticada.
- As rotas são protegidas com base no papel do usuário:
- `USER`: cria chamados, vê seus chamados.
- `TECHNICIAN`: vê chamados não atribuídos e chamados atribuídos a si.
- `ADMIN`: gerencia usuários, papéis e vê todos os chamados.

A proteção é feita com anotações como `@PreAuthorize("hasRole('TECHNICIAN')")` nos controllers.

---

## 🧪 Descrição dos Testes Implementados

### ✅ Testes Unitários

- Cobertura de regras de negócio em `UserService`, `TicketService`
- Verificação de comportamento com Mockito (`@Mock`, `@InjectMocks`)
- Validação de DTOs (campos obrigatórios, enums)

### ✅ Testes Funcionais

- Implementados com `MockMvc` simulando chamadas HTTP reais
- Casos cobertos:
- Registro e login de usuário
- Abertura de chamado
- Atribuição a técnico
- Resolução de chamado
- Verificações de autorização (usuário comum não acessa rotas de técnico/admin)
