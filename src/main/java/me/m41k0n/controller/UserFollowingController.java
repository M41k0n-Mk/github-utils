package me.m41k0n.controller;

import me.m41k0n.model.User;
import me.m41k0n.service.GitHubService;
import me.m41k0n.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/following")
@CrossOrigin(origins = "*")
public class UserFollowingController {

    private final GitHubService gitHubService;

    public UserFollowingController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    /**
     * GET /api/user/following — retorna uma página de usuários que você segue (following) diretamente do GitHub.
     * Somente leitura. Não persiste em banco.
     *
     * Parâmetros:
     * - page: número da página (default 1)
     * - size: tamanho da página (default 30)
     *
     * Resposta 200 (application/json):
     * { "page": number, "size": number, "users": User[] }
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listFollowing(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "30") int size) {
        List<User> users = gitHubService.getFollowing(page, size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("page", page);
        resp.put("size", size);
        resp.put("users", users);
        return ResponseEntity.ok(resp);
    }
    /**
     * DELETE /api/user/following/{username} — desfaz o follow (unfollow) de um usuário.
     * Respeita o modo dry-run: quando ativo, não executa a escrita e apenas registra em histórico.
     *
     * Path variable:
     * - username: login do usuário alvo
     *
     * Resposta 200 (application/json): { "message": string, "dryRun": boolean }
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, Object>> unfollow(@PathVariable String username) {
        boolean dryRun = gitHubService.unfollow(username, null);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Unfollowed " + username);
        resp.put("dryRun", dryRun);
        return ResponseEntity.ok(resp);
    }
    /**
     * PUT /api/user/following/{username} — segue (follow) um usuário.
     * Respeita o modo dry-run: quando ativo, não executa a escrita e apenas registra em histórico.
     *
     * Path variable:
     * - username: login do usuário alvo
     *
     * Resposta 200 (application/json): { "message": string, "dryRun": boolean }
     */
    @PutMapping("/{username}")
    public ResponseEntity<Map<String, Object>> follow(@PathVariable String username) {
        boolean dryRun = gitHubService.follow(username, null);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Followed " + username);
        resp.put("dryRun", dryRun);
        return ResponseEntity.ok(resp);
    }
}
