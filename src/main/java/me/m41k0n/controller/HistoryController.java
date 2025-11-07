package me.m41k0n.controller;

import me.m41k0n.entity.HistoryEntity;
import me.m41k0n.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<List<HistoryEntity>> getHistory(@RequestParam(required = false) String username,
                                                          @RequestParam(required = false) String action,
                                                          @RequestParam(required = false) String since) {
        Instant sinceInstant = since != null && !since.isBlank() ? Instant.parse(since) : null;
        return ResponseEntity.ok(historyService.search(username, action, sinceInstant));
    }
}
