package me.m41k0n.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.model.User;
import me.m41k0n.service.GitHubService;

import java.util.List;

public class GetNonFollowersCommand implements Command<List<User>> {

    private final GitHubService gitHubService;

    public GetNonFollowersCommand(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Override
    public List<User> execute() throws JsonProcessingException {
        List<User> nonFollowers = gitHubService.getNonFollowers();
        nonFollowers.forEach(System.out::println);
        return nonFollowers;
    }
}