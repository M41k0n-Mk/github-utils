package me.m41k0n.controller;

import me.m41k0n.service.UndoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/undo")
@CrossOrigin(origins = "*")
public class UndoController {

    private final UndoService undoService;

    public UndoController(UndoService undoService) {
        this.undoService = undoService;
    }

    public record UndoRequest(String until, List<String> usernames, String action) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> undo(@RequestBody(required = false) UndoRequest req) {
        Instant untilInstant = undoService.resolveUntilInstant(req != null ? req.until : null);
        undoService.validateAction(req != null ? req.action : null);
        Set<String> filterUsernames = (req != null && req.usernames != null) ? new HashSet<>(req.usernames) : null;
        UndoService.Result result = undoService.processUndo(untilInstant, filterUsernames);
        return ResponseEntity.ok(buildUndoResponse(result));
    }

    private Map<String, Object> buildUndoResponse(UndoService.Result r) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("refollowed", r.refollowed());
        resp.put("details", r.details());
        resp.put("dryRun", r.dryRun());
        return resp;
    }
}
