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
        Instant untilInstant = parseUntilInstant(req);
        validateUndoAction(req);

        List<HistoryEntity> events = historyService.findUnfollowsSince(untilInstant);
        Set<String> filterUsernames = req != null && req.usernames != null ? new HashSet<>(req.usernames) : null;

        var result = processUndoEvents(events, filterUsernames);
        return ResponseEntity.ok(buildUndoResponse(result));
    }

    // ===== Helpers extraídos para legibilidade =====
    private static Instant parseUntilInstant(UndoRequest req) {
        if (req != null && req.until != null && !req.until.isBlank()) {
            return Instant.parse(req.until);
        }
        return Instant.now().minus(60, ChronoUnit.MINUTES); // valor real aplicado abaixo via defaultMinutes
    }

    private void validateUndoAction(UndoRequest req) {
        String action = (req != null && req.action != null) ? req.action : "unfollow";
        if (!"unfollow".equalsIgnoreCase(action)) {
            throw new IllegalArgumentException("Only 'unfollow' actions can be undone");
        }
    }

    private static class UndoResult {
        final int refollowed;
        final List<Map<String, Object>> details;
        final boolean dryRun;
        UndoResult(int refollowed, List<Map<String, Object>> details, boolean dryRun) {
            this.refollowed = refollowed; this.details = details; this.dryRun = dryRun;
        }
    }

    private UndoResult processUndoEvents(List<HistoryEntity> events, Set<String> filterUsernames) {
        int refollowed = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        boolean dryRun = false;
        for (HistoryEntity e : events) {
            String username = e.getUsername();
            if (filterUsernames != null && !filterUsernames.contains(username)) continue;
            boolean opDry = gitHubService.follow(username, null);
            dryRun = dryRun || opDry;
            refollowed++;
            Map<String, Object> d = new HashMap<>();
            d.put("username", username);
            d.put("timestamp", Instant.now().toString());
            details.add(d);
        }
        return new UndoResult(refollowed, details, dryRun);
    }

    private Map<String, Object> buildUndoResponse(UndoResult r) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("refollowed", r.refollowed);
        resp.put("details", r.details);
        resp.put("dryRun", r.dryRun);
        return resp;
    }
}
