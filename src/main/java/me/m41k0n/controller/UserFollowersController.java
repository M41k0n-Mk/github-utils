package me.m41k0n.controller;

import me.m41k0n.model.User;
import me.m41k0n.service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/followers")
@CrossOrigin(origins = "*")
public class UserFollowersController {

    private final GitHubService gitHubService;

    public UserFollowersController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listFollowers(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "30") int size) {
        List<User> users = gitHubService.getFollowers(page, size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("page", page);
        resp.put("size", size);
        resp.put("users", users);
        return ResponseEntity.ok(resp);
    }
}
