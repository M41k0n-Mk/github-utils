# Arquitetura da Aplicação

## Visão Geral

```
┌─────────────────────────────────────────────────────────────┐
│                     GitHub Utils Application                 │
│                                                              │
│  ┌──────────────┐                    ┌──────────────┐      │
│  │  Menu Mode   │                    │   API Mode   │      │
│  │   (--menu)   │                    │  (default)   │      │
│  └──────┬───────┘                    └──────┬───────┘      │
│         │                                    │               │
│         │                                    │               │
│         v                                    v               │
│  ┌─────────────┐                    ┌─────────────┐        │
│  │    Main     │                    │ Application │        │
│  │   (Console) │                    │(Spring Boot)│        │
│  └──────┬──────┘                    └──────┬──────┘        │
│         │                                    │               │
│         │                                    │               │
│         └────────────┬───────────────────────┘              │
│                      │                                       │
│                      v                                       │
│            ┌──────────────────┐                             │
│            │  GitHubService   │                             │
│            │ (Business Logic) │                             │
│            └─────────┬────────┘                             │
│                      │                                       │
│                      v                                       │
│            ┌──────────────────┐                             │
│            │   APIConsume     │                             │
│            │ (HTTP Client)    │                             │
│            └─────────┬────────┘                             │
│                      │                                       │
│                      │ GITHUB_TOKEN                          │
└──────────────────────┼───────────────────────────────────────┘
                       │
                       v
              ┌─────────────────┐
              │   GitHub API    │
              │ api.github.com  │
              └─────────────────┘
```

## Componentes

### 1. Camada de Entrada (Entry Points)

#### Menu Mode (`--menu`)
- **Propósito:** Interface de linha de comando interativa
- **Entrada:** `Main.runMenuMode()`
- **Saída:** Console (stdout)
- **Uso:** Operações manuais rápidas

#### API Mode (default)
- **Propósito:** REST API HTTP
- **Entrada:** `Application.main()` → Spring Boot
- **Saída:** Respostas JSON
- **Uso:** Integração com frontend

### 2. Camada de Apresentação

#### Main (Console)
```
Main.java
├── runMenuMode()
├── showMenu()
└── MenuContext
    ├── NonFollowersCommand
    ├── UnfollowCommand
    └── ExitApplicationAction
```

#### GitHubController (REST)
```
GitHubController.java
├── GET  /api/followers/health
├── GET  /api/non-followers
└── DELETE /api/followers/unfollow-non-followers
```

### 3. Camada de Negócio

#### GitHubService
```
GitHubService.java
├── getNonFollowers()
│   ├── getFollowers()
│   └── getFollowing()
└── unfollowNonFollowers()
```

**Responsabilidades:**
- Lógica de comparação de seguidores
- Orquestração de chamadas à API do GitHub
- Processamento de dados

### 4. Camada de Acesso a Dados

#### APIConsume
```
APIConsume.java
├── getData(url)
└── deleteData(url)
```

**Responsabilidades:**
- Comunicação HTTP com GitHub API
- Gerenciamento de autenticação (Bearer Token)
- Tratamento de erros HTTP

### 5. Configuração

#### AppConfig
```
AppConfig.java
├── httpClient() → Bean HttpClient
└── corsConfigurer() → CORS Configuration
```

#### application.properties
```
server.port=8080
logging.level.root=INFO
```

## Fluxo de Dados

### Menu Mode
```
Usuário → Console
    → Main.showMenu()
        → MenuContext.executeAction()
            → NonFollowersCommand.execute()
                → GitHubService (implicitamente via lógica duplicada)
                    → APIConsume.getData()
                        → GitHub API
                            → Resposta
                                → Exibição no Console
```

### API Mode
```
Frontend/cURL → HTTP Request
    → GitHubController
        → GitHubService.getNonFollowers()
            → APIConsume.getData()
                → GitHub API
                    → Resposta JSON
                        → GitHubController
                            → HTTP Response (JSON)
```

## Modelos de Dados

### User
```java
public record User(
    String login,      // Nome de usuário do GitHub
    String html_url    // URL do perfil
)
```

### Response Models (API Mode)

#### Non-Followers Response
```json
{
  "count": 5,
  "users": [User, User, ...]
}
```

#### Success Response
```json
{
  "message": "string"
}
```

#### Error Response
```json
{
  "error": "string",
  "message": "string"
}
```

## Dependências Externas

### GitHub API
- **Endpoint Followers:** `https://api.github.com/user/followers`
- **Endpoint Following:** `https://api.github.com/user/following`
- **Autenticação:** Bearer Token (GITHUB_TOKEN)
- **Rate Limit:** 5000 req/hora (autenticado)

### Spring Boot
- **spring-boot-starter-web:** Framework REST
- **Tomcat Embedded:** Servidor web
- **Jackson:** Serialização JSON

### Java HTTP Client
- **java.net.http.HttpClient:** Cliente HTTP nativo (Java 11+)

## Segurança

### Token Management
```
Environment Variable: GITHUB_TOKEN
    ↓
System.getenv("GITHUB_TOKEN")
    ↓
APIConsume (Authorization Header)
    ↓
GitHub API
```

### CORS
```
CORS Configuration
├── Allowed Origins: * (desenvolvimento)
├── Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
└── Allowed Headers: *
```

⚠️ **Nota:** Em produção, configure origens específicas.

## Escalabilidade

### Limitações Atuais
- ✅ Token único (um usuário por instância)
- ✅ Sem cache
- ✅ Sem persistência
- ✅ Paginação limitada (100 itens)

### Melhorias Futuras
- [ ] Multi-tenancy (múltiplos usuários)
- [ ] Cache Redis
- [ ] Banco de dados para histórico
- [ ] Paginação completa
- [ ] Queue para processamento assíncrono
- [ ] Webhooks do GitHub

## Deployment

### Desenvolvimento
```bash
mvn spring-boot:run
# ou
java -jar target/github-utils-1.0-SNAPSHOT.jar
```

### Produção
```bash
# Build
mvn clean package -DskipTests

# Deploy
java -jar github-utils-1.0-SNAPSHOT.jar \
  --server.port=80 \
  --spring.profiles.active=prod
```

### Docker (Futuro)
```dockerfile
FROM openjdk:17-slim
COPY target/*.jar app.jar
ENV GITHUB_TOKEN=${GITHUB_TOKEN}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Monitoramento

### Logs
```
logging.level.root=INFO
logging.level.me.m41k0n=DEBUG
```

### Health Check
```bash
curl http://localhost:8080/api/followers/health
```

### Métricas (Futuro)
- [ ] Spring Boot Actuator
- [ ] Prometheus
- [ ] Grafana
