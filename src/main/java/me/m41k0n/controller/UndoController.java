package me.m41k0n.controller;

import me.m41k0n.entity.HistoryEntity;
import me.m41k0n.service.GitHubService;
import me.m41k0n.service.HistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/undo")
@CrossOrigin(origins = "*")
public class UndoController {

    private final HistoryService historyService;
    private final GitHubService gitHubService;
    private final int defaultMinutes;

    public UndoController(HistoryService historyService,
                          GitHubService gitHubService,
                          @Value("${app.undo.defaultMinutes:60}") int defaultMinutes) {
        this.historyService = historyService;
        this.gitHubService = gitHubService;
        this.defaultMinutes = defaultMinutes;
    }

    public record UndoRequest(String until, List<String> usernames, String action) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> undo(@RequestBody(required = false) UndoRequest req) {
        Instant untilInstant;
        if (req != null && req.until != null && !req.until.isBlank()) {
            untilInstant = Instant.parse(req.until);
        } else {
            untilInstant = Instant.now().minus(defaultMinutes, ChronoUnit.MINUTES);
        }
        String action = (req != null && req.action != null) ? req.action : "unfollow";
        if (!"unfollow".equalsIgnoreCase(action)) {
            throw new IllegalArgumentException("Only 'unfollow' actions can be undone");
        }

        List<HistoryEntity> events = historyService.findUnfollowsSince(untilInstant);
        Set<String> filterUsernames = req != null && req.usernames != null ? new HashSet<>(req.usernames) : null;

        int refollowed = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        boolean dryRun = false;
        for (HistoryEntity e : events) {
            String username = e.getUsername();
            if (filterUsernames != null && !filterUsernames.contains(username)) {
                continue;
            }
            boolean opDry = gitHubService.follow(username, null);
            dryRun = dryRun || opDry;
            refollowed++;
            Map<String, Object> d = new HashMap<>();
            d.put("username", username);
            d.put("timestamp", Instant.now().toString());
            details.add(d);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("refollowed", refollowed);
        resp.put("details", details);
        resp.put("dryRun", dryRun);
        return ResponseEntity.ok(resp);
    }
}
