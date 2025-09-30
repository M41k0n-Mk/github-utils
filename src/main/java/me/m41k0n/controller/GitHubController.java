package me.m41k0n.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.model.User;
import me.m41k0n.service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/followers")
@CrossOrigin(origins = "*")
public class GitHubController {
    
    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

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

    @DeleteMapping("/unfollow-non-followers")
    public ResponseEntity<Map<String, String>> unfollowNonFollowers() {
        try {
            gitHubService.unfollowNonFollowers();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Unfollow realizado com sucesso em todos que não te seguem");
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
