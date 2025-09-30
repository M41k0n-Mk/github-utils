package me.m41k0n.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.GitHubURL;
import me.m41k0n.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubService {
    private final APIConsume apiConsume;
    private final ObjectMapper mapper = new ObjectMapper();

    public GitHubService(APIConsume apiConsume) {
        this.apiConsume = apiConsume;
    }

    public List<User> getNonFollowers() throws JsonProcessingException {
        List<User> followers = getFollowers();
        List<User> following = getFollowing();
        
        following.removeAll(followers);
        return following;
    }

    public void unfollowNonFollowers() throws JsonProcessingException {
        List<User> nonFollowers = getNonFollowers();
        nonFollowers.forEach(u -> apiConsume.deleteData(GitHubURL.FOLLOWING.getUrl() + "/" + u.login()));
    }

    private List<User> getFollowers() throws JsonProcessingException {
        String followers = apiConsume.getData(GitHubURL.FOLLOWERS.getUrl());
        return mapper.readValue(followers, new TypeReference<>() {
        });
    }

    private List<User> getFollowing() throws JsonProcessingException {
        String following = apiConsume.getData(GitHubURL.FOLLOWING.getUrl() + "?per_page=100");
        return mapper.readValue(following, new TypeReference<>() {
        });
    }
}
