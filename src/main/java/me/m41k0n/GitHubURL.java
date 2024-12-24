package me.m41k0n;

public enum GitHubURL {
    FOLLOWERS("https://api.github.com/user/followers"),
    FOLLOWING("https://api.github.com/user/following");

    private final String url;

    GitHubURL(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
