package me.m41k0n.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.service.GitHubService;

public class MassUnfollowCommand implements Command<Void> {
    private final GitHubService gitHubService;

    public MassUnfollowCommand(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Override
    public Void execute() throws JsonProcessingException {
        gitHubService.unfollowNonFollowers();
        return null;
    }
}