package me.m41k0n.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service to manage dry-run state and provide status information.
 * Centralizes dry-run logic. Reads initial value from application properties, but allows runtime toggle.
 */
@Service
public class DryRunService {

    private static final String LEGACY_SYS_PROP = "app.dryRun"; // backward compat for CLI flag
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private volatile boolean enabled;
    private LocalDateTime lastToggleTime;

    public DryRunService(@Value("${app.dry-run:true}") boolean defaultEnabled) {
        // Prefer explicit system property if present (CLI flag support), else use application property
        String sys = System.getProperty(LEGACY_SYS_PROP);
        if (sys != null) {
            this.enabled = Boolean.parseBoolean(sys);
        } else {
            this.enabled = defaultEnabled;
        }
    }

    public boolean isDryRunEnabled() {
        return enabled;
    }

    public void enableDryRun() {
        this.enabled = true;
        this.lastToggleTime = LocalDateTime.now();
    }

    public void disableDryRun() {
        this.enabled = false;
        this.lastToggleTime = LocalDateTime.now();
    }

    public boolean toggleDryRun() {
        this.enabled = !this.enabled;
        this.lastToggleTime = LocalDateTime.now();
        return this.enabled;
    }

    public DryRunStatus getStatus() {
        return new DryRunStatus(
            isDryRunEnabled(),
            lastToggleTime != null ? lastToggleTime.format(TIMESTAMP_FORMAT) : "Never changed"
        );
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