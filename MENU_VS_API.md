# Comparação: Menu vs API

Esta aplicação oferece duas formas de uso. Escolha a que melhor se adapta às suas necessidades.

## Tabela Comparativa

| Característica | Modo Menu | Modo API |
|----------------|-----------|----------|
| **Interface** | Terminal interativo | HTTP REST API |
| **Uso** | Manual, uma operação por vez | Pode ser integrado em aplicações web/mobile |
| **Inicialização** | `java -jar app.jar --menu` | `java -jar app.jar` |
| **Melhor para** | Uso pessoal rápido | Integração com frontend |
| **Requer servidor** | ❌ Não | ✅ Sim (porta 8080) |
| **Interface gráfica** | ❌ Não | ✅ Pode ter (via frontend) |
| **Automação** | ❌ Limitada | ✅ Total |
| **Múltiplos usuários** | ❌ Não | ✅ Sim (com modificações) |

## Quando usar cada modo?

### Use o Modo Menu quando:
- ✅ Você quer fazer uma limpeza rápida nos seguidores
- ✅ Prefere usar o terminal
- ✅ Não precisa de interface gráfica
- ✅ Quer executar rapidamente sem configurar servidor

### Use o Modo API quando:
- ✅ Quer criar uma interface web personalizada
- ✅ Precisa integrar com outras aplicações
- ✅ Quer acessar de múltiplos dispositivos
- ✅ Deseja automatizar com scripts
- ✅ Planeja adicionar mais funcionalidades no futuro

## Exemplos Práticos

### Cenário 1: Uso Pessoal Rápido
**Modo Menu** é ideal:
```bash
# Inicia, escolhe opção, sai
java -jar github-utils.jar --menu
```

### Cenário 2: Dashboard Web
**Modo API** é ideal:
```bash
# Inicia servidor
java -jar github-utils.jar

# Frontend faz requisições
# Usuário acessa via navegador em qualquer dispositivo
```

### Cenário 3: Automação com Script
**Modo API** é ideal:
```bash
# Servidor em background
nohup java -jar github-utils.jar &

# Script automatizado
curl http://localhost:8080/api/followers/non-followers | \
  jq '.count' | \
  mail -s "Você tem X não-seguidores" seu@email.com
```

## Fluxo de Trabalho Recomendado

### Para Desenvolvimento Frontend
1. **Backend (este projeto):**
   ```bash
   java -jar github-utils.jar
   ```
2. **Frontend (Angular/React/Vue):**
   - Consome os endpoints da API
   - Cria interface gráfica bonita
   - Adiciona features extras (filtros, busca, etc)

### Para Uso Pessoal Simples
```bash
# Uma linha, resultados imediatos
echo "1" | java -jar github-utils.jar --menu
```

## Migração entre Modos

Você pode começar com o modo menu e depois migrar para API quando precisar:

**Fase 1 - Teste (Menu):**
```bash
java -jar github-utils.jar --menu
```

**Fase 2 - Desenvolvimento (API Local):**
```bash
java -jar github-utils.jar
# Desenvolve frontend localmente
```

**Fase 3 - Produção (API + Frontend):**
```bash
# Deploy do backend
# Deploy do frontend
# Frontend conecta no backend
```

## Executando Ambos

Sim, você pode ter os dois ao mesmo tempo!

**Terminal 1 - API:**
```bash
java -jar github-utils.jar
# Servidor na porta 8080
```

**Terminal 2 - Menu:**
```bash
java -jar github-utils.jar --menu
# Uso via terminal
```

Ambos compartilham o mesmo token do GitHub configurado na variável de ambiente.
