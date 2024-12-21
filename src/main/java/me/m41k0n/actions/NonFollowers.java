package me.m41k0n.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.model.User;
import me.m41k0n.service.APIConsume;

import java.util.List;

public class NonFollowers implements MenuAction {
    private final APIConsume apiConsume;
    public static final String URL_FOLLOWERS = "https://api.github.com/user/followers";
    public static final String URL_FOLLOWING = "https://api.github.com/user/following";
    ObjectMapper mapper = new ObjectMapper();

    public NonFollowers(APIConsume apiConsume) {
        this.apiConsume = apiConsume;
    }

    @Override
    public void execute() throws JsonProcessingException {
        List<User> followers = getFollowers();
        List<User> following = getFollowing();

        following.removeAll(followers);
        following.forEach(System.out::println);
    }

    private List<User> getFollowers() throws JsonProcessingException {
        String followers = apiConsume.getData(URL_FOLLOWERS);
        return mapper.readValue(followers, new TypeReference<>() {
        });
    }

    private List<User> getFollowing() throws JsonProcessingException {
        String following = apiConsume.getData(URL_FOLLOWING);
        return mapper.readValue(following, new TypeReference<>() {
        });
    }
}