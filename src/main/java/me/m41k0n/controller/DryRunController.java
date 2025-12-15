package me.m41k0n.controller;

import me.m41k0n.service.DryRunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller dedicated to dry-run operations.
 * Separates dry-run concerns from GitHub operations.
 */
@RestController
@RequestMapping("/api/dry-run")
@CrossOrigin(origins = "*")
public class DryRunController {
    
    private final DryRunService dryRunService;
    
    public DryRunController(DryRunService dryRunService) {
        this.dryRunService = dryRunService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDryRunStatus() {
        DryRunService.DryRunStatus status = dryRunService.getStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", status.isEnabled());
        response.put("description", status.getDescription());
        response.put("lastChanged", status.getLastChanged());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleDryRun() {
        boolean newState = dryRunService.toggleDryRun();
        DryRunService.DryRunStatus status = dryRunService.getStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", newState);
        response.put("description", status.getDescription());
        response.put("message", "Dry-run mode " + (newState ? "enabled" : "disabled"));
        
        return ResponseEntity.ok(response);
    }
    @PostMapping("/enable")
    public ResponseEntity<Map<String, Object>> enableDryRun() {
        dryRunService.enableDryRun();
        DryRunService.DryRunStatus status = dryRunService.getStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", true);
        response.put("description", status.getDescription());
        response.put("message", "Dry-run mode enabled");
        
        return ResponseEntity.ok(response);
    }
    @PostMapping("/disable")
    public ResponseEntity<Map<String, Object>> disableDryRun() {
        dryRunService.disableDryRun();
        DryRunService.DryRunStatus status = dryRunService.getStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", false);
        response.put("description", status.getDescription());
        response.put("message", "Dry-run mode disabled");
        
        return ResponseEntity.ok(response);
    }
}