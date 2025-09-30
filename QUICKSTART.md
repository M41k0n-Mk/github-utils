# Guia Rápido de Início

## Instalação Rápida (5 minutos)

### 1. Pré-requisitos
```bash
# Verifique se tem Java 17+
java --version

# Verifique se tem Maven
mvn --version
```

### 2. Clone e Compile
```bash
# Clone
git clone https://github.com/M41k0n-Mk/github-utils.git
cd github-utils

# Compile
mvn clean package -DskipTests
```

### 3. Configure o Token
```bash
# Gere seu token em: https://github.com/settings/tokens
# Selecione o scope: user:follow

# Configure (escolha seu sistema):
export GITHUB_TOKEN=seu_token_aqui           # Linux/Mac
set GITHUB_TOKEN=seu_token_aqui              # Windows CMD
$env:GITHUB_TOKEN="seu_token_aqui"           # Windows PowerShell
```

### 4. Execute

#### Modo Menu (Simples):
```bash
java -jar target/github-utils-1.0-SNAPSHOT.jar --menu
```

#### Modo API (para Frontend):
```bash
java -jar target/github-utils-1.0-SNAPSHOT.jar
```

Acesse: http://localhost:8080/api/followers/health

---

## Teste Rápido

### Menu Mode:
```bash
# Execute
java -jar target/github-utils-1.0-SNAPSHOT.jar --menu

# Digite 1 para ver não-seguidores
# Digite 0 para sair
```

### API Mode:
```bash
# Terminal 1: Inicie o servidor
java -jar target/github-utils-1.0-SNAPSHOT.jar

# Terminal 2: Teste os endpoints
curl http://localhost:8080/api/followers/health
curl http://localhost:8080/api/followers/non-followers
```

---

## Comandos Úteis

### Build
```bash
# Build completo
mvn clean package

# Build rápido (sem testes)
mvn clean package -DskipTests

# Apenas compilar
mvn compile
```

### Execução
```bash
# Modo Menu
java -jar target/github-utils-1.0-SNAPSHOT.jar --menu

# Modo API
java -jar target/github-utils-1.0-SNAPSHOT.jar

# Com Maven (desenvolvimento)
mvn spring-boot:run                                    # API
mvn spring-boot:run -Dspring-boot.run.arguments="--menu"  # Menu
```

### Testes
```bash
# Health check
curl http://localhost:8080/api/followers/health

# Ver não-seguidores
curl http://localhost:8080/api/followers/non-followers

# Unfollow (cuidado!)
curl -X DELETE http://localhost:8080/api/followers/unfollow-non-followers
```

---

## Problemas Comuns

### "Token não configurado"
**Problema:** Variável de ambiente não está definida  
**Solução:**
```bash
export GITHUB_TOKEN=seu_token_aqui
# Execute o comando novamente
```

### "Port 8080 already in use"
**Problema:** Porta já está sendo usada  
**Solução:**
```bash
# Opção 1: Mate o processo na porta 8080
lsof -ti:8080 | xargs kill -9  # Mac/Linux
netstat -ano | findstr :8080   # Windows (anote o PID)
taskkill /PID <PID> /F         # Windows

# Opção 2: Use outra porta
java -jar target/github-utils-1.0-SNAPSHOT.jar --server.port=8081
```

### "Java version not compatible"
**Problema:** Java 16 ou menor  
**Solução:** Instale Java 17+
```bash
# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# Mac
brew install openjdk@17

# Windows
# Baixe de: https://adoptium.net/
```

---

## Próximos Passos

### Se você quer usar via Menu:
1. ✅ Configure o token
2. ✅ Execute com `--menu`
3. ✅ Use as opções do menu

### Se você quer usar via API:
1. ✅ Configure o token
2. ✅ Execute sem `--menu`
3. ✅ Desenvolva seu frontend
4. ✅ Consuma os endpoints

### Para desenvolver o Frontend Angular:
```bash
# Clone o frontend
git clone https://github.com/M41k0n-Mk/github-utils-frontend.git
cd github-utils-frontend

# Configure o backend URL
# Geralmente em: src/environments/environment.ts
# apiUrl: 'http://localhost:8080'

# Execute
npm install
ng serve
```

Acesse: http://localhost:4200

---

## Recursos

- **README.md** - Documentação completa
- **API_DOCUMENTATION.md** - Referência da API
- **MENU_VS_API.md** - Comparação entre modos

## Suporte

Encontrou um problema? [Abra uma issue](https://github.com/M41k0n-Mk/github-utils/issues)
