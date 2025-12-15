package me.m41k0n.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.service.ExclusionService;
import me.m41k0n.service.GitHubService;
import me.m41k0n.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "*")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    private final GitHubService gitHubService;
    private final ExclusionService exclusionService;
    private final HistoryService historyService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ImportController(GitHubService gitHubService, ExclusionService exclusionService, HistoryService historyService) {
        this.gitHubService = gitHubService;
        this.exclusionService = exclusionService;
        this.historyService = historyService;
    }

    // Endpoints legados mantidos (use /api/import/users com action=refollow|exclude)
    /**
     * LEGACY: importe usernames e execute refollow.
     * Prefira usar: POST /api/import/users?action=refollow
     */
    @Deprecated
    @PostMapping(value = "/refollow", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Map<String, Object>> importRefollow(@RequestBody String body) {
        return handleRefollow(usernamesFromBody(body), true);
    }

    /**
     * LEGACY: importe usernames e adicione-os na lista de exclusão.
     * Prefira usar: POST /api/import/users?action=exclude
     */
    @Deprecated
    @PostMapping(value = "/exclude", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Map<String, Object>> importExclude(@RequestBody String body) {
        return handleExclude(usernamesFromBody(body));
    }

    // Endpoint unificado com multipart CSV/JSON
    @PostMapping(value = "/users", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    /**
     * POST /api/import/users — endpoint unificado de importação.
     *
     * Descrição: recebe uma lista de usernames via arquivo (multipart CSV/JSON, campo "file") ou
     * no corpo da requisição (text/plain CSV ou application/json array de strings) e executa a
     * ação especificada por query param:
     * - action=exclude → adiciona os usuários à lista especial de exclusão (EXCLUDE_NEXT_RUN)
     * - action=refollow → executa follow para os usuários informados
     *
     * Parâmetros (query):
     * - action: "refollow" | "exclude" (obrigatório)
     * - skipProcessed: boolean (default true) — só se aplica à ação refollow; quando true, evita
     *   repetir follow para usuários que já foram efetivamente seguidos no passado (histórico)
     *
     * Entradas suportadas:
     * - multipart/form-data com campo "file" (.csv ou .json)
     * - text/plain (CSV) no corpo, com cabeçalho opcional "login"
     * - application/json no corpo: array de strings com usernames
     *
     * Resposta 200 (application/json):
     * - action=exclude → { received, added }
     * - action=refollow → { received, applied, skipped, dryRun, details[] }
     */
    public ResponseEntity<Map<String, Object>> importUsers(@RequestParam String action,
                                                           @RequestParam(defaultValue = "true") boolean skipProcessed,
                                                           @RequestPart(value = "file", required = false) MultipartFile file,
                                                           @RequestBody(required = false) String body) {
        List<String> usernames = (file != null) ? usernamesFromFile(file) : usernamesFromBody(body);
        action = action == null ? "" : action.trim().toLowerCase();
        log.info("[IMPORT] action={} count={}", action, usernames.size());
        return switch (action) {
            case "exclude" -> handleExclude(usernames);
            case "refollow" -> handleRefollow(usernames, skipProcessed);
            default -> ResponseEntity.badRequest().body(Map.of("error", "Invalid action. Use 'refollow' or 'exclude'"));
        };
    }

    // ===== helpers =====
    private ResponseEntity<Map<String, Object>> handleExclude(List<String> usernames) {
        List<String> norm = normalizeUsernames(usernames);
        int added = exclusionService.addAll(norm);
        return ResponseEntity.ok(Map.of(
                "received", norm.size(),
                "added", added
        ));
    }

    private ResponseEntity<Map<String, Object>> handleRefollow(List<String> usernames, boolean skipProcessed) {
        List<String> norm = normalizeUsernames(usernames);
        boolean dryRun = gitHubService.isDryRunEnabled();
        int applied = 0;
        int skipped = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (String u : norm) {
            if (u == null || u.isBlank()) continue;
            if (skipProcessed && historyService.alreadyProcessed(u, "follow")) {
                skipped++;
                details.add(detail(u, "skipped", "already-follow"));
                continue;
            }
            try {
                boolean opDry = gitHubService.follow(u, "IMPORT_REFOLLOW");
                applied++;
                details.add(detail(u, opDry ? "dry-run" : "followed", null));
            } catch (Exception e) {
                details.add(detail(u, "error", e.getMessage()));
                log.warn("[IMPORT-REFOLLOW] Falha para {}: {}", u, e.getMessage());
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("received", norm.size());
        resp.put("applied", applied);
        resp.put("skipped", skipped);
        resp.put("dryRun", dryRun);
        resp.put("details", details);
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> detail(String username, String status, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("username", username);
        m.put("status", status);
        if (message != null) m.put("message", message);
        return m;
    }

    private List<String> usernamesFromFile(MultipartFile file) {
        try {
            String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (filename.endsWith(".json") || looksLikeJson(content)) {
                return usernamesFromJson(content);
            }
            return parseUsernamesFromCsv(content);
        } catch (Exception e) {
            log.warn("[IMPORT] Falha ao ler arquivo: {}", e.getMessage());
            return List.of();
        }
    }

    private boolean looksLikeJson(String s) {
        String t = s == null ? "" : s.trim();
        return t.startsWith("[") && t.endsWith("]");
    }

    private List<String> usernamesFromBody(String body) {
        if (body == null || body.isBlank()) return List.of();
        String trimmed = body.trim();
        if (looksLikeJson(trimmed)) return usernamesFromJson(trimmed);
        return parseUsernamesFromCsv(trimmed);
    }

    private List<String> usernamesFromJson(String json) {
        try {
            List<String> arr = new ObjectMapper().readValue(json, new TypeReference<List<String>>(){});
            return arr == null ? List.of() : arr;
        } catch (Exception e) {
            log.warn("[IMPORT] JSON inválido: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> parseUsernamesFromCsv(String body) {
        if (body == null || body.isBlank()) return List.of();
        List<String> result = new ArrayList<>();
        String[] lines = body.split("\r?\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank()) continue;
            // ignora cabeçalho comum
            if (i == 0 && (line.toLowerCase().startsWith("login,") || line.equalsIgnoreCase("login"))) {
                continue; // ignora cabeçalho
            }
            String[] parts = line.split(",");
            String username = parts[0].trim();
            if (!username.isBlank()) {
                result.add(username);
            }
        }
        return result;
    }
    private List<String> normalizeUsernames(List<String> usernames) {
        if (usernames == null) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String u : usernames) {
            if (u == null) continue;
            String v = u.trim();
            if (!v.isBlank()) set.add(v);
        }
        return new ArrayList<>(set);
    }
}
