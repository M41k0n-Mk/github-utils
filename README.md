# GitHub Utils

Uma aplicação Java para gerenciar seus seguidores no GitHub, permitindo identificar quem você segue mas não te segue de volta, e fazer unfollow em massa dessas contas.

## 📋 Funcionalidades

- **Listar não-seguidores**: Obtém uma lista de usuários que você segue mas que não te seguem de volta
- **Unfollow em massa**: Remove o follow de todos os usuários que não te seguem de volta
- **Interface Dupla**: Utilize via menu de texto no console OU via API REST

## 🚀 Modos de Uso

Esta aplicação pode ser utilizada de duas formas:

### 1. Menu de Texto (Console)
Interface interativa no terminal com menu de opções.

### 2. API REST
Endpoints HTTP para integração com aplicações frontend (como Angular, React, etc).

## 🔧 Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- Token de acesso pessoal do GitHub (PAT) com permissões:
  - `user:follow` - para gerenciar seguidores

## ⚙️ Configuração

### 1. Gerar Token do GitHub

1. Acesse: https://github.com/settings/tokens
2. Clique em "Generate new token (classic)"
3. Selecione o scope `user:follow`
4. Copie o token gerado

### 2. Configurar Token como Variável de Ambiente

**Linux/Mac:**
```bash
export GITHUB_TOKEN=seu_token_aqui
```

**Windows (CMD):**
```cmd
set GITHUB_TOKEN=seu_token_aqui
```

**Windows (PowerShell):**
```powershell
$env:GITHUB_TOKEN="seu_token_aqui"
```

## 📦 Instalação

```bash
# Clone o repositório
git clone https://github.com/M41k0n-Mk/github-utils.git
cd github-utils

# Compile o projeto
mvn clean package
```

## 🎯 Como Usar

### Modo 1: Menu de Texto (Console)

Execute a aplicação com a flag `--menu`:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--menu"
```

Ou usando o JAR compilado:

```bash
java -jar target/github-utils-1.0-SNAPSHOT.jar --menu
```

**Menu de Opções:**
```
1 - Obter uma lista de quem você segue mas não segue você
2 - Dar unfollow em todos que não te seguem
0 - Sair
```

### Modo 2: API REST

Execute a aplicação sem argumentos para iniciar o servidor REST:

```bash
mvn spring-boot:run
```

Ou usando o JAR compilado:

```bash
java -jar target/github-utils-1.0-SNAPSHOT.jar
```

O servidor iniciará na porta `8080` por padrão.

## 📡 Endpoints da API

### Health Check
Verifica se a API está funcionando.

```http
GET http://localhost:8080/api/followers/health
```

**Resposta:**
```json
{
  "status": "UP",
  "service": "GitHub Utils API"
}
```

### Listar Não-Seguidores
Retorna lista de usuários que você segue mas não te seguem de volta.

```http
GET http://localhost:8080/api/followers/non-followers
```

**Resposta:**
```json
{
  "count": 5,
  "users": [
    {
      "login": "usuario1",
      "html_url": "https://github.com/usuario1"
    },
    {
      "login": "usuario2",
      "html_url": "https://github.com/usuario2"
    }
  ]
}
```

### Unfollow em Massa
Remove o follow de todos que não te seguem de volta.

```http
DELETE http://localhost:8080/api/followers/unfollow-non-followers
```

**Resposta:**
```json
{
  "message": "Unfollow realizado com sucesso em todos que não te seguem"
}
```

## 🌐 Integração com Frontend

Esta API está pronta para ser consumida por aplicações frontend. Exemplo de integração:

### Frontend Angular
Repositório do frontend: [@M41k0n-Mk/github-utils-frontend](https://github.com/M41k0n-Mk/github-utils-frontend)

A API possui CORS habilitado para permitir requisições de qualquer origem. Em produção, recomenda-se configurar origens específicas.

### Exemplo de Requisição com Fetch (JavaScript)

```javascript
// Listar não-seguidores
fetch('http://localhost:8080/api/followers/non-followers')
  .then(response => response.json())
  .then(data => console.log(data));

// Unfollow em massa
fetch('http://localhost:8080/api/followers/unfollow-non-followers', {
  method: 'DELETE'
})
  .then(response => response.json())
  .then(data => console.log(data));
```

### Exemplo com Angular HttpClient

```typescript
import { HttpClient } from '@angular/common/http';

constructor(private http: HttpClient) {}

getNonFollowers() {
  return this.http.get('http://localhost:8080/api/followers/non-followers');
}

unfollowNonFollowers() {
  return this.http.delete('http://localhost:8080/api/followers/unfollow-non-followers');
}
```

## 🏗️ Estrutura do Projeto

```
github-utils/
├── src/main/java/me/m41k0n/
│   ├── Application.java          # Aplicação Spring Boot
│   ├── Main.java                 # Ponto de entrada com suporte a menu
│   ├── config/
│   │   └── AppConfig.java        # Configurações (HttpClient, CORS)
│   ├── controller/
│   │   └── GitHubController.java # Controllers REST
│   ├── service/
│   │   ├── APIConsume.java       # Cliente HTTP para API do GitHub
│   │   └── GitHubService.java    # Lógica de negócio
│   ├── model/
│   │   └── User.java             # Modelo de dados
│   └── actions/                  # Comandos do menu (modo console)
├── src/main/resources/
│   └── application.properties    # Configurações do Spring Boot
└── pom.xml                       # Dependências Maven
```

## 🛠️ Tecnologias Utilizadas

- **Java 17**: Linguagem de programação
- **Spring Boot 3.1.5**: Framework para criação da API REST
- **Jackson**: Serialização/deserialização JSON
- **Maven**: Gerenciamento de dependências e build
- **HttpClient (Java 11+)**: Cliente HTTP nativo para comunicação com GitHub API

## ⚠️ Avisos Importantes

1. **Rate Limiting**: A API do GitHub possui limites de requisições. Com autenticação, você tem até 5000 requisições por hora.

2. **Paginação**: A aplicação busca até 100 seguindo/seguidores. Para contas com mais seguidores, pode ser necessário implementar paginação adicional.

3. **Unfollow em Massa**: Use com cuidado! A operação de unfollow em massa é irreversível.

## 📝 Licença

Este projeto é de código aberto e está disponível sob a licença MIT.

## 👤 Autor

**M41k0n-Mk**
- GitHub: [@M41k0n-Mk](https://github.com/M41k0n-Mk)

## 🤝 Contribuindo

Contribuições, issues e pull requests são bem-vindos!

## 📞 Suporte

Se você encontrar algum problema ou tiver dúvidas, por favor abra uma [issue](https://github.com/M41k0n-Mk/github-utils/issues).
