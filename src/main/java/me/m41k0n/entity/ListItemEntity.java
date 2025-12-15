package me.m41k0n.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "list_items")
@IdClass(ListItemId.class)
public class ListItemEntity {

    @Id
    @Column(name = "list_id", nullable = false)
    private String listId;

    @Id
    @Column(name = "username", nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", insertable = false, updatable = false)
    private ListEntity list;

    public void setListId(String listId) { this.listId = listId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public ListEntity getList() { return list; }
    public void setList(ListEntity list) { this.list = list; }
}
