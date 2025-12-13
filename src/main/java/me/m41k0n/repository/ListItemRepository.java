package me.m41k0n.repository;

import me.m41k0n.entity.ListItemEntity;
import me.m41k0n.entity.ListItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListItemRepository extends JpaRepository<ListItemEntity, ListItemId> {
    List<ListItemEntity> findByListId(String listId);
    void deleteByListId(String listId);
}
