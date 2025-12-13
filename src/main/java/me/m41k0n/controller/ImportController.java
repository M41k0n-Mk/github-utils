package me.m41k0n.controller;

import me.m41k0n.service.ExclusionService;
import me.m41k0n.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "*")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    private final GitHubService gitHubService;
    private final ExclusionService exclusionService;

    public ImportController(GitHubService gitHubService, ExclusionService exclusionService) {
        this.gitHubService = gitHubService;
        this.exclusionService = exclusionService;
    }

    @PostMapping(value = "/refollow", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Map<String, Object>> importRefollow(@RequestBody String csvBody) {
        List<String> usernames = parseUsernamesFromCsv(csvBody);
        int total = usernames.size();
        int executed = 0;
        boolean dryRun = gitHubService.isDryRunEnabled();
        List<Map<String, Object>> details = new ArrayList<>();

        for (String u : usernames) {
            if (u == null || u.isBlank()) continue;
            Map<String, Object> d = new HashMap<>();
            d.put("username", u);
            try {
                boolean opDry = gitHubService.follow(u, "IMPORT_REFOLLOW");
                d.put("status", opDry ? "dry-run" : "followed");
                if (!opDry) executed++;
                log.info("[IMPORT-REFOLLOW] {} {}", opDry ? "(dry-run) would follow" : "followed", u);
            } catch (Exception e) {
                d.put("status", "error");
                d.put("message", e.getMessage());
                log.warn("[IMPORT-REFOLLOW] Falha para {}: {}", u, e.getMessage());
            }
            details.add(d);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("total", total);
        resp.put("executed", executed);
        resp.put("dryRun", dryRun);
        resp.put("details", details);
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/exclude", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Map<String, Object>> importExclude(@RequestBody String csvBody) {
        List<String> usernames = parseUsernamesFromCsv(csvBody);
        int added = exclusionService.addAll(usernames);
        Map<String, Object> resp = new HashMap<>();
        resp.put("received", usernames.size());
        resp.put("added", added);
        return ResponseEntity.ok(resp);
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
                continue;
            }
            String[] parts = line.split(",");
            String username = parts[0].trim();
            if (!username.isBlank()) {
                result.add(username);
            }
        }
        return result;
    }
}
