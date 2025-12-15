package me.m41k0n.repository;

import me.m41k0n.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface HistoryRepository extends JpaRepository<HistoryEntity, String> {

    boolean existsByUsernameAndActionAndDryRun(String username, String action, int dryRun);

    List<HistoryEntity> findByActionAndDryRunAndTimestampGreaterThanEqual(String action, int dryRun, String timestampIso);

    @Query("select h from HistoryEntity h where (:username is null or h.username = :username) and (:action is null or h.action = :action) and (:sinceIso is null or h.timestamp >= :sinceIso) and h.dryRun = 0")
    List<HistoryEntity> search(String username, String action, String sinceIso);
}
