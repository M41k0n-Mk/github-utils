package me.m41k0n.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.GitHubURL;
import me.m41k0n.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubService {

    private static final int MAX_PAGES = 100;
    private static final int PER_PAGE = 100;

    private final APIConsume apiConsume;
    private final DryRunService dryRunService;
    private final ObjectMapper mapper = new ObjectMapper();

    public GitHubService(APIConsume apiConsume, DryRunService dryRunService) {
        this.apiConsume = apiConsume;
        this.dryRunService = dryRunService;
    }

    public List<User> getNonFollowers() throws JsonProcessingException {
        System.out.println("🔄 Getting complete followers and following lists...");
        
        PreviewReport report = previewNonFollowers(1, Integer.MAX_VALUE);
        List<User> allNonFollowers = new ArrayList<>();
        
        // Extract all users from the single page (contains all since pageSize is MAX_VALUE)
        allNonFollowers.addAll(report.getPage());
        
        System.out.println("📊 Complete Stats - Followers: " + report.getTotalFollowers() + ", Following: " + report.getTotalFollowing());
        System.out.println("📊 Non-followers result: " + allNonFollowers.size());

        return allNonFollowers;
    }

    public void unfollowNonFollowers() throws JsonProcessingException {
        List<User> nonFollowers = getNonFollowers();
        System.out.println("🚀 Starting mass unfollow for " + nonFollowers.size() + " users");

        if (dryRunService.isDryRunEnabled()) {
            System.out.println("ℹ️ DRY-RUN enabled: no unfollow requests will be made.\nList of targets:");
            nonFollowers.forEach(user -> System.out.println("Would unfollow: " + user.login()));
            System.out.println("✅ DRY-RUN preview complete");
            return;
        }

        nonFollowers.forEach(user -> {
            System.out.println("Unfollowing: " + user.login());
            apiConsume.deleteData(GitHubURL.FOLLOWING.getUrl() + "/" + user.login());
        });

        System.out.println("✅ Mass unfollow completed");
    }

    /**
     * Check if dry-run mode is enabled
     */
    public boolean isDryRunEnabled() {
        return dryRunService.isDryRunEnabled();
    }

    /**
     * Returns a preview report with pagination for non-followers. Only performs GET requests.
     */
    public PreviewReport previewNonFollowers(int pageNumber, int pageSize) throws JsonProcessingException {
        List<User> followers = getAllUsers(GitHubURL.FOLLOWERS, "followers");
        List<User> following = getAllUsers(GitHubURL.FOLLOWING, "following");

        List<User> nonFollowers = new ArrayList<>(following);
        nonFollowers.removeAll(followers);

        int totalNon = nonFollowers.size();

        int from = Math.max(0, (pageNumber - 1) * pageSize);
        int to = Math.min(totalNon, from + pageSize);
        List<User> page = from >= to ? new ArrayList<>() : nonFollowers.subList(from, to);

        return new PreviewReport(followers.size(), following.size(), totalNon, page, pageNumber, pageSize);
    }

    /**
     * Generic method to fetch all users with pagination
     * Eliminates code duplication between followers and following
     */
    private List<User> getAllUsers(GitHubURL urlType, String userType) throws JsonProcessingException {
        List<User> allUsers = new ArrayList<>();
        int page = 1;

        System.out.println("🔄 Fetching all " + userType + " with pagination...");

        while (page <= MAX_PAGES) {
            String url = urlType.getUrl() + "?per_page=" + PER_PAGE + "&page=" + page;
            System.out.println("📄 Fetching " + userType + " page " + page);

            String response = apiConsume.getData(url);
            List<User> pageUsers = mapper.readValue(response, new TypeReference<>() {});

            System.out.println("📊 Page " + page + " returned " + pageUsers.size() + " " + userType);

            if (pageUsers.isEmpty()) {
                System.out.println("✅ No more " + userType + " pages to fetch");
                break;
            }

            allUsers.addAll(pageUsers);
            page++;
        }

        if (page > MAX_PAGES) {
            System.out.println("⚠️ Reached maximum page limit (" + MAX_PAGES + ") for " + userType);
        }

        System.out.println("✅ Total " + userType + " fetched: " + allUsers.size());
        return allUsers;
    }
}