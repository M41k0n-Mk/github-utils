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
    private final HistoryService historyService;

    public UserFollowingController(GitHubService gitHubService, HistoryService historyService) {
        this.gitHubService = gitHubService;
        this.historyService = historyService;
    }

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

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, Object>> unfollow(@PathVariable String username) {
        boolean dryRun = gitHubService.unfollow(username, null);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Unfollowed " + username);
        resp.put("dryRun", dryRun);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{username}")
    public ResponseEntity<Map<String, Object>> follow(@PathVariable String username) {
        boolean dryRun = gitHubService.follow(username, null);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Followed " + username);
        resp.put("dryRun", dryRun);
        return ResponseEntity.ok(resp);
    }
}
