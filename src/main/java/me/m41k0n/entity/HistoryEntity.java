package me.m41k0n.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "history")
public class HistoryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "action", nullable = false)
    private String action; // 'follow' | 'unfollow'

    @Column(name = "timestamp", nullable = false)
    private String timestamp; // ISO8601

    @Column(name = "source_list_id")
    private String sourceListId; // nullable

    @Column(name = "dry_run", nullable = false)
    private int dryRun = 0; // 0/1

    @PrePersist
    public void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.timestamp == null) this.timestamp = Instant.now().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getSourceListId() { return sourceListId; }
    public void setSourceListId(String sourceListId) { this.sourceListId = sourceListId; }
    public int getDryRun() { return dryRun; }
    public void setDryRun(int dryRun) { this.dryRun = dryRun; }
}
