package me.m41k0n.repository;

import me.m41k0n.entity.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ListRepository extends JpaRepository<ListEntity, String> {

    Optional<ListEntity> findByName(String name);
}
