package me.m41k0n.repository;

import me.m41k0n.entity.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListRepository extends JpaRepository<ListEntity, String> {
}
