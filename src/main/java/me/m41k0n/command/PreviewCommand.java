package me.m41k0n.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.model.User;
import me.m41k0n.service.ExportUtils;
import me.m41k0n.service.GitHubService;
import me.m41k0n.service.PreviewReport;

import java.util.List;
import java.util.Scanner;

public class PreviewCommand implements Command<Void> {
    private final GitHubService gitHubService;

    public PreviewCommand(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Override
    public Void execute() throws JsonProcessingException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Page number (default 1): ");
        String p = scanner.nextLine().trim();
        int page = p.isEmpty() ? 1 : Integer.parseInt(p);
        System.out.print("Page size (default 25): ");
        String s = scanner.nextLine().trim();
        int size = s.isEmpty() ? 25 : Integer.parseInt(s);

        try {
            PreviewReport report = gitHubService.previewNonFollowers(page, size);
            System.out.println("Followers: " + report.getTotalFollowers() + ", Following: " + report.getTotalFollowing() + ", Non-followers: " + report.getTotalNonFollowers());
            List<User> users = report.getPage();
            users.forEach(u -> System.out.println(u.login() + " - " + u.html_url()));

            System.out.print("Export? (none/csv/json): ");
            String fmt = scanner.nextLine().trim().toLowerCase();
            if ("csv".equals(fmt)) {
                String csv = ExportUtils.toCsv(users);
                System.out.println("--- CSV ---\n" + csv);
            } else if ("json".equals(fmt)) {
                String json = ExportUtils.toJson(users);
                System.out.println("--- JSON ---\n" + json);
            }
        } catch (Exception e) {
            System.out.println("Erro ao obter preview: " + e.getMessage());
        }

        return null;
    }
}
