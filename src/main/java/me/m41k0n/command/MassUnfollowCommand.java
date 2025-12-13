package me.m41k0n.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.service.GitHubService;
import me.m41k0n.service.PreviewReport;

import java.util.Scanner;
import java.util.stream.Collectors;

public class MassUnfollowCommand implements Command<Void> {

    private final GitHubService gitHubService;

    public MassUnfollowCommand(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Override
    public Void execute() throws JsonProcessingException {
        // Show preview and ask for confirmation in CLI mode
        PreviewReport report = gitHubService.previewNonFollowers(1, 10);
        String summary = String.format("Followers=%d, Following=%d, Non-followers=%d (showing %d)", 
            report.getTotalFollowers(), report.getTotalFollowing(), 
            report.getTotalNonFollowers(), report.getPage().size());

        System.out.println(summary);
        System.out.println("Sample targets: " + report.getPage().stream()
            .map(u -> u.login()).limit(5).collect(Collectors.joining(", ")));
        
        // First confirmation
        System.out.print("Are you sure you want to unfollow all non-followers? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().toLowerCase();
        
        if (!response.equals("yes") && !response.equals("y")) {
            System.out.println("Operation cancelled by user.");
            return null;
        }

        // Second-chance confirmation with more emphasis
        System.out.println("\n⚠️ FINAL CONFIRMATION ⚠️");
        System.out.println("This will unfollow " + report.getTotalNonFollowers() + " users!");
        System.out.print("Type 'CONFIRM' to proceed or anything else to cancel: ");
        String finalResponse = scanner.nextLine();
        
        if (!"CONFIRM".equals(finalResponse)) {
            System.out.println("Operation cancelled by user - second chance taken.");
            return null;
        }

        gitHubService.unfollowNonFollowers();
        return null;
    }
}