package me.m41k0n.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.model.User;
import me.m41k0n.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GitHubController {
    
    private final GitHubService gitHubService;
    private final ExportService exportService;

    public GitHubController(GitHubService gitHubService, ExportService exportService) {
        this.gitHubService = gitHubService;
        this.exportService = exportService;
    }
//TODO duvida pq na aplicação front tem fazendo request p esse aqui mas com reuest params
    @GetMapping("/non-followers")
    public ResponseEntity<Map<String, Object>> getNonFollowers() {
        try {
            List<User> nonFollowers = gitHubService.getNonFollowers();
            Map<String, Object> response = new HashMap<>();
            response.put("count", nonFollowers.size());
            response.put("users", nonFollowers);
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao processar resposta da API do GitHub");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
//ok
    @GetMapping("/non-followers/preview")
    public ResponseEntity<?> previewNonFollowers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String format) {
        try {
            var report = gitHubService.previewNonFollowers(page, size);

            // Handle export formats
            ExportService.ExportFormat exportFormat = ExportService.ExportFormat.fromString(format);
            if (exportFormat != null) {
                String exportedData = exportService.exportToFormat(report.getPage(), exportFormat);
                return ResponseEntity.ok()
                    .header("Content-Type", exportFormat.getMimeType())
                    .header("Content-Disposition", String.format("attachment; filename=\"non-followers-page-%d.%s\"", page, format.toLowerCase()))
                    .body(exportedData);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("totalFollowers", report.getTotalFollowers());
            resp.put("totalFollowing", report.getTotalFollowing());
            resp.put("totalNonFollowers", report.getTotalNonFollowers());
            resp.put("page", report.getPageNumber());
            resp.put("size", report.getPageSize());
            resp.put("users", report.getPage());
            resp.put("dryRunEnabled", gitHubService.isDryRunEnabled());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao gerar preview");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
//ok
    @DeleteMapping("/unfollow-non-followers")
    public ResponseEntity<Map<String, String>> unfollowNonFollowers() {
        try {
            gitHubService.unfollowNonFollowers();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Unfollow realizado com sucesso em todos que não te seguem");
            response.put("warning", "⚠️ Use o endpoint /api/unfollow/execute para operações mais seguras com confirmação");
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao processar resposta da API do GitHub");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "GitHub Utils API");
        return ResponseEntity.ok(response);
    }
}
