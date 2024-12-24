package me.m41k0n.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.GitHubURL;
import me.m41k0n.model.User;
import me.m41k0n.service.APIConsume;

import java.util.List;

public class NonFollowersCommand implements MenuAction<List<User>> {
    private final APIConsume apiConsume;
    ObjectMapper mapper = new ObjectMapper();

    public NonFollowersCommand(APIConsume apiConsume) {
        this.apiConsume = apiConsume;
    }

    @Override
    public List<User> execute() throws JsonProcessingException {
        List<User> followers = getFollowers();
        List<User> following = getFollowing();

        following.removeAll(followers);
        following.forEach(System.out::println);
        return following;
    }

    private List<User> getFollowers() throws JsonProcessingException {
        String followers = apiConsume.getData(GitHubURL.FOLLOWERS.getUrl());
        return mapper.readValue(followers, new TypeReference<>() {
        });
    }

    private List<User> getFollowing() throws JsonProcessingException {
        String following = apiConsume.getData(GitHubURL.FOLLOWING.getUrl());
        return mapper.readValue(following, new TypeReference<>() {
        });
    }
}