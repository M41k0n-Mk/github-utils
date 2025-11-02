package me.m41k0n.service;

import me.m41k0n.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service to manage dry-run state and provide status information.
 * Centralized dry-run logic instead of scattered System.getProperty calls.
 */
@Service
public class DryRunService {
    
    private static final String DRY_RUN_PROPERTY = "app.dryRun";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private LocalDateTime lastToggleTime;
    
    public boolean isDryRunEnabled() {
        return Boolean.parseBoolean(System.getProperty(DRY_RUN_PROPERTY, "false"));
    }
    
    public void enableDryRun() {
        System.setProperty(DRY_RUN_PROPERTY, "true");
        this.lastToggleTime = LocalDateTime.now();
    }
    
    public void disableDryRun() {
        System.setProperty(DRY_RUN_PROPERTY, "false");
        this.lastToggleTime = LocalDateTime.now();
    }
    
    public boolean toggleDryRun() {
        boolean currentState = isDryRunEnabled();
        if (currentState) {
            disableDryRun();
        } else {
            enableDryRun();
        }
        return !currentState; // return new state
    }
    
    public DryRunStatus getStatus() {
        return new DryRunStatus(
            isDryRunEnabled(),
            lastToggleTime != null ? lastToggleTime.format(TIMESTAMP_FORMAT) : "Never changed"
        );
    }
    
    public String formatUnfollowAction(User user) {
        if (isDryRunEnabled()) {
            return "Would unfollow: " + user.login();
        } else {
            return "Unfollowing: " + user.login();
        }
    }
    
    /**
     * Status object for dry-run information
     */
    public static class DryRunStatus {
        private final boolean enabled;
        private final String lastChanged;
        
        public DryRunStatus(boolean enabled, String lastChanged) {
            this.enabled = enabled;
            this.lastChanged = lastChanged;
        }
        
        public boolean isEnabled() { return enabled; }
        public String getLastChanged() { return lastChanged; }
        public String getDescription() { 
            return enabled ? "DRY-RUN enabled (no writes)" : "DRY-RUN disabled (writes allowed)";
        }
    }
}