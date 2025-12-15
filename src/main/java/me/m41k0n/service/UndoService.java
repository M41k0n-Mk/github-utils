package me.m41k0n.service;

import me.m41k0n.entity.HistoryEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class UndoService {

    private final HistoryService historyService;
    private final GitHubService gitHubService;
    private final int defaultMinutes;

    public UndoService(HistoryService historyService,
                       GitHubService gitHubService,
                       @Value("${app.undo.defaultMinutes:60}") int defaultMinutes) {
        this.historyService = historyService;
        this.gitHubService = gitHubService;
        this.defaultMinutes = defaultMinutes;
    }

    public Instant resolveUntilInstant(String untilIso) {
        if (untilIso != null && !untilIso.isBlank()) {
            return Instant.parse(untilIso);
        }
        return Instant.now().minus(defaultMinutes, ChronoUnit.MINUTES);
    }

    public void validateAction(String action) {
        String a = (action != null) ? action : "unfollow";
        if (!"unfollow".equalsIgnoreCase(a)) {
            throw new IllegalArgumentException("Only 'unfollow' actions can be undone");
        }
    }

    public Result processUndo(Instant since, Set<String> usernamesFilter) {
        List<HistoryEntity> events = historyService.findUnfollowsSince(since);
        int refollowed = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        boolean dryRun = false;
        for (HistoryEntity e : events) {
            String username = e.getUsername();
            if (usernamesFilter != null && !usernamesFilter.contains(username)) continue;
            boolean opDry = gitHubService.follow(username, null);
            dryRun = dryRun || opDry;
            refollowed++;
            Map<String, Object> d = new HashMap<>();
            d.put("username", username);
            d.put("timestamp", Instant.now().toString());
            details.add(d);
        }
        return new Result(refollowed, details, dryRun);
    }

    public record Result(int refollowed, List<Map<String, Object>> details, boolean dryRun) {}
}
