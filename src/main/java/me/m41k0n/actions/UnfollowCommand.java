package me.m41k0n.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.GitHubURL;
import me.m41k0n.model.User;
import me.m41k0n.service.APIConsume;

import java.util.List;

public class UnfollowCommand implements MenuAction<Void> {
    private final APIConsume apiConsume;
    private final NonFollowersCommand nonFollowersCommand;
    ObjectMapper mapper = new ObjectMapper();

    public UnfollowCommand(APIConsume apiConsume, NonFollowersCommand nonFollowersCommand) {
        this.apiConsume = apiConsume;
        this.nonFollowersCommand = nonFollowersCommand;
    }

    @Override
    public Void execute() throws JsonProcessingException {
        List<User> nonFollowers = nonFollowersCommand.execute();
        nonFollowers.forEach(u -> apiConsume.deleteData(GitHubURL.FOLLOWING.getUrl() + "/" + u.login()));
        return null;
    }
}