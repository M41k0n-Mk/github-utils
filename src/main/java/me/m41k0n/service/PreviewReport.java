package me.m41k0n.service;

import me.m41k0n.model.User;

import java.util.List;

public class PreviewReport {
    
    private final int totalFollowers;
    private final int totalFollowing;
    private final int totalNonFollowers;
    private final List<User> page;
    private final int pageNumber;
    private final int pageSize;

    public PreviewReport(int totalFollowers, int totalFollowing, int totalNonFollowers, List<User> page, int pageNumber, int pageSize) {
        this.totalFollowers = totalFollowers;
        this.totalFollowing = totalFollowing;
        this.totalNonFollowers = totalNonFollowers;
        this.page = page;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public int getTotalFollowers() { return totalFollowers; }
    public int getTotalFollowing() { return totalFollowing; }
    public int getTotalNonFollowers() { return totalNonFollowers; }
    public List<User> getPage() { return page; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
}
