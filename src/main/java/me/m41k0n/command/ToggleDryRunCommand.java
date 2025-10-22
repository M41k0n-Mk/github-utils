package me.m41k0n.command;

import me.m41k0n.service.DryRunService;

public class ToggleDryRunCommand implements Command<Void> {
    
    private final DryRunService dryRunService;
    
    public ToggleDryRunCommand(DryRunService dryRunService) {
        this.dryRunService = dryRunService;
    }
    
    @Override
    public Void execute() {
        boolean newState = dryRunService.toggleDryRun();
        DryRunService.DryRunStatus status = dryRunService.getStatus();
        System.out.println("Dry-run is now " + (newState ? "ENABLED (no writes)" : "DISABLED (writes allowed)"));
        System.out.println("Status: " + status.getDescription());
        return null;
    }
}
