# API Documentation

## Base URL
```
http://localhost:8080
```

## Endpoints

### 1. Health Check
Verifica se a API está funcionando corretamente.

**Endpoint:** `GET /api/followers/health`

**Response:**
```json
{
  "status": "UP",
  "service": "GitHub Utils API"
}
```

**Status Code:** `200 OK`

---

### 2. Listar Não-Seguidores
Retorna uma lista de usuários que você segue mas que não te seguem de volta.

**Endpoint:** `GET /api/followers/non-followers`

**Headers Required:**
- Nenhum (o token do GitHub é lido da variável de ambiente `GITHUB_TOKEN`)

**Response Success:**
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

**Status Code:** `200 OK`

**Response Error:**
```json
{
  "error": "Erro ao processar resposta da API do GitHub",
  "message": "Detalhes do erro..."
}
```

**Status Code:** `500 Internal Server Error`

---

### 3. Unfollow em Massa
Remove o follow de todos os usuários que não te seguem de volta.

**Endpoint:** `DELETE /api/followers/unfollow-non-followers`

**Headers Required:**
- Nenhum (o token do GitHub é lido da variável de ambiente `GITHUB_TOKEN`)

**Response Success:**
```json
{
  "message": "Unfollow realizado com sucesso em todos que não te seguem"
}
```

**Status Code:** `200 OK`

**Response Error:**
```json
{
  "error": "Erro ao processar resposta da API do GitHub",
  "message": "Detalhes do erro..."
}
```

**Status Code:** `500 Internal Server Error`

---

## CORS
A API possui CORS configurado para aceitar requisições de qualquer origem (`*`). Em produção, é recomendado configurar origens específicas.

**Headers CORS:**
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS`
- `Access-Control-Allow-Headers: *`

---

## Exemplos de Uso

### cURL

**Health Check:**
```bash
curl http://localhost:8080/api/followers/health
```

**Listar Não-Seguidores:**
```bash
curl http://localhost:8080/api/followers/non-followers
```

**Unfollow em Massa:**
```bash
curl -X DELETE http://localhost:8080/api/followers/unfollow-non-followers
```

### JavaScript (Fetch API)

```javascript
// Health Check
fetch('http://localhost:8080/api/followers/health')
  .then(response => response.json())
  .then(data => console.log(data));

// Listar Não-Seguidores
fetch('http://localhost:8080/api/followers/non-followers')
  .then(response => response.json())
  .then(data => {
    console.log(`Total: ${data.count}`);
    console.log(data.users);
  });

// Unfollow em Massa
fetch('http://localhost:8080/api/followers/unfollow-non-followers', {
  method: 'DELETE'
})
  .then(response => response.json())
  .then(data => console.log(data.message));
```

### Angular HttpClient

```typescript
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GitHubUtilsService {
  private baseUrl = 'http://localhost:8080/api/followers';

  constructor(private http: HttpClient) {}

  // Health Check
  healthCheck(): Observable<any> {
    return this.http.get(`${this.baseUrl}/health`);
  }

  // Listar Não-Seguidores
  getNonFollowers(): Observable<any> {
    return this.http.get(`${this.baseUrl}/non-followers`);
  }

  // Unfollow em Massa
  unfollowNonFollowers(): Observable<any> {
    return this.http.delete(`${this.baseUrl}/unfollow-non-followers`);
  }
}
```

### React (usando axios)

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/followers';

// Health Check
const healthCheck = async () => {
  const response = await axios.get(`${API_BASE_URL}/health`);
  return response.data;
};

// Listar Não-Seguidores
const getNonFollowers = async () => {
  const response = await axios.get(`${API_BASE_URL}/non-followers`);
  return response.data;
};

// Unfollow em Massa
const unfollowNonFollowers = async () => {
  const response = await axios.delete(`${API_BASE_URL}/unfollow-non-followers`);
  return response.data;
};
```

---

## Autenticação

A aplicação utiliza o token de acesso pessoal do GitHub (PAT) para autenticar as requisições à API do GitHub.

**Configuração:**
1. Gere um token em: https://github.com/settings/tokens
2. Selecione o scope `user:follow`
3. Configure a variável de ambiente:

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

---

## Rate Limiting

A API do GitHub possui limites de requisições:
- **Com autenticação:** 5000 requisições por hora
- **Sem autenticação:** 60 requisições por hora

Para verificar seus limites atuais, acesse: https://api.github.com/rate_limit

---

## Erros Comuns

### 401 Unauthorized
**Causa:** Token do GitHub inválido ou ausente  
**Solução:** Verifique se a variável de ambiente `GITHUB_TOKEN` está configurada corretamente

### 403 Forbidden
**Causa:** Rate limit excedido  
**Solução:** Aguarde o reset do rate limit (1 hora)

### 500 Internal Server Error
**Causa:** Erro ao processar resposta da API do GitHub  
**Solução:** Verifique os logs do servidor para mais detalhes
