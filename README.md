# API Biblioteca

API REST para gerenciamento de uma biblioteca, desenvolvida com Spring Boot e PostgreSQL. O sistema permite cadastrar usuários, consultar livros e autores, e gerenciar empréstimos.

## Tecnologias

- Java
- Spring Boot
- Spring Security
- PostgreSQL
- Redis / Memurai
- JWT (autenticação por token)
- Swagger / OpenAPI
- Maven

## Pré-requisitos

Antes de executar o projeto, você precisa ter instalado:

- JDK compatível com a versão Java configurada no projeto
- PostgreSQL
- Um servidor compatível com Redis, como Memurai ou Redis, configurado para aceitar conexões no endereço e na porta definidos em application.properties.
- Git

## Como executar

### 1. Clone o repositório

```bash
git clone https://github.com/Victor-Rei-07/api-biblioteca.git
cd api-biblioteca
```

### 2. Configure o banco de dados

Crie o banco de dados `library` no PostgreSQL e execute o script `script.sql` utilizando uma ferramenta de administração do PostgreSQL, como o pgAdmin ou o `psql`.

Verifique se o banco está acessível e se as credenciais utilizadas correspondem às configurações do ambiente.

### 3. Configure as variáveis de ambiente

A aplicação utiliza as seguintes variáveis de ambiente:

| Variável | Descrição | Exemplo |
|---|---|---|
| `DB_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/biblioteca` |
| `DB_USERNAME` | Usuário do PostgreSQL | `postgres` |
| `DB_PASSWORD` | Senha do PostgreSQL | Sua senha |
| `JWT_SECRET` | Chave usada para assinar tokens JWT | Chave secreta gerada aleatoriamente |

As variáveis devem estar disponíveis no ambiente em que a aplicação será executada. Um arquivo `.env` comum não é carregado automaticamente pelo Spring Boot.

Exemplo no PowerShell (Windows):

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/biblioteca"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="sua_senha"
$env:JWT_SECRET="sua_chave_secreta"
```

Exemplo no Bash (Linux, macOS ou Git Bash):

```bash
export DB_URL="jdbc:postgresql://localhost:5432/biblioteca"
export DB_USERNAME="postgres"
export DB_PASSWORD="sua_senha"
export JWT_SECRET="sua_chave_secreta"
```

Os comandos acima definem as variáveis para a sessão atual do terminal. Execute a aplicação no mesmo terminal.

**Importante:** gere uma chave JWT aleatória para seu ambiente. Nunca publique senhas, chaves secretas ou arquivos contendo credenciais no GitHub.

### 4. Inicie o Redis

Certifique-se de que o Redis esteja em execução e acessível conforme as configurações da aplicação.

### 5. Execute a aplicação

No Linux, macOS ou Git Bash:

```bash
./mvnw spring-boot:run
```

No Windows, usando o PowerShell ou CMD:

```powershell
.\mvnw.cmd spring-boot:run
```

Por padrão, a aplicação estará disponível em:

http://localhost:8080

## Documentação da API

Com a aplicação em execução, acesse a interface Swagger:

http://localhost:8080/swagger-ui/index.html

Nela, você pode consultar os endpoints disponíveis e testar as requisições da API.

## Funcionalidades

- Cadastro de usuários com hash de senha utilizando BCrypt
- Login e autenticação por JWT
- Autorização de acesso aos recursos protegidos
- Tratamento centralizado de erros
- Consulta de usuários, livros, autores e empréstimos
- Realização e devolução de empréstimos
- Utilização de Redis para armazenamento em cache
