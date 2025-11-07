package me.m41k0n.entity;

import java.io.Serializable;
import java.util.Objects;

public class ListItemId implements Serializable {
    private String listId;
    private String username;

    public ListItemId() {}

    public ListItemId(String listId, String username) {
        this.listId = listId;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItemId that = (ListItemId) o;
        return Objects.equals(listId, that.listId) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listId, username);
    }
}
